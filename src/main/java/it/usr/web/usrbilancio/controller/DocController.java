/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoCodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoRecord;
import it.usr.web.usrbilancio.domain.tables.records.MovimentiVirtualiRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.domain.tables.records.RichiestaRecord;
import it.usr.web.usrbilancio.producer.DocumentFolder;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.MovimentiVirtualiService;
import it.usr.web.usrbilancio.service.Mutables;
import it.usr.web.usrbilancio.service.OrdinativoAppoggioService;
import it.usr.web.usrbilancio.service.OrdinativoService;
import it.usr.web.usrbilancio.service.QuietanzaService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ApplicationScoped
public class DocController extends BaseController {
    @Inject
    QuietanzaService qs;
    @Inject
    OrdinativoService os;
    @Inject
    OrdinativoAppoggioService oas;
    @Inject
    MovimentiVirtualiService mvs;
    @Inject
    CompetenzaService cs;
    @Inject
    CodiceService codServ;
    @Inject
    @AppLogger
    Logger logger; 
    @DocumentFolder
    @Inject
    String documentFolder;
    
    public StreamedContent getStream() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE || context.getCurrentPhaseId() == PhaseId.INVOKE_APPLICATION) {
            return new DefaultStreamedContent();
        } else {
            boolean empty = Boolean.parseBoolean(context.getExternalContext().getRequestParameterMap().get("empty"));
            if(empty) return getEmptyDocument();
                    
            // So, browser is requesting the media. Return a real StreamedContent with the media bytes.
            Mutables.MutableString nomeFile = new Mutables.MutableString();
            Mutables.MutableString nomeFileLocale = new Mutables.MutableString();
            
            String scope = null;
            Integer id = null;
            try {
                scope = context.getExternalContext().getRequestParameterMap().get("scope");
                String s = context.getExternalContext().getRequestParameterMap().get("id");
                if(isInteger(s)) {
                    id = Integer.valueOf(s);
                }  
                else {
                    logger.error("Non riesco a generare il documento con tipologia [{}] e id=[{}].", scope, id);
                    return getNotFoundDocument(scope, id);
                }
                                                   
                switch(scope) {
                    case "Q" ->  {
                        QuietanzaRecord q = qs.getQuietanzaById(id);
                        nomeFile.s = q.getNomefile();
                        nomeFileLocale.s = q.getNomefileLocale();
                    }
                    case "O" ->  {
                        AllegatoRecord a = os.getAllegatoById(id);
                        nomeFile.s = a.getNomefile();
                        nomeFileLocale.s = a.getNomefileLocale();
                    }
                    case "OA" ->  {
                        AllegatoAppoggioRecord a = oas.getAllegatoAppoggioById(id);
                        nomeFile.s = a.getNomefile();
                        nomeFileLocale.s = a.getNomefileLocale();
                    }
                    case "MV" ->  {
                        MovimentiVirtualiRecord mv = mvs.getMovimentoVirtualeById(id);
                        nomeFile.s = mv.getNomefile();
                        nomeFileLocale.s = mv.getNomefileLocale();
                    }
                    case "R" ->  {
                        RichiestaRecord r = cs.getRichiestaById(id);
                        nomeFile.s = r.getNomefile();
                        nomeFileLocale.s = r.getNomefileLocale();
                    }
                    case "C" -> {
                        AllegatoCodiceRecord ac = codServ.getAllegatoCodiceById(id);
                        nomeFile.s = ac.getNomefile();
                        nomeFileLocale.s = ac.getNomefileLocale();
                    }
                    default -> {
                        logger.error("Non riesco a generare il documento con tipologia [{}] e id=[{}].", scope, id);
                        return getNotFoundDocument(scope, id!=null ? id : -1);
                    }
                }
            }
            catch(NullPointerException npe) {
                logger.error("Non riesco a preparate il documento di tipologia [{}] e id [{}] perchè non trovato nel database. Invio documento di notifica.", scope, id);
                return getNotFoundDocument(scope, id!=null ? id : -1);
            }
            catch(NumberFormatException e) {
                logger.error("Non riesco a preparate il documento di tipologia [{}] e id [{}] a causa di {}", scope, id, getStackTrace(e));
                return getErrorDocument();                
            }
 
            try {                            
                if(FilenameUtils.getExtension(nomeFileLocale.s).equalsIgnoreCase("pdf")) {
                    byte[] data = Files.readAllBytes(Paths.get(documentFolder+"/"+nomeFileLocale.s));
                    logger.info("Generazione stream documento [{}] tipologia [{}] id [{}] nome [{}] dimensione [{}].", nomeFileLocale, scope, id, nomeFile, data.length);
                    InputStream is = new ByteArrayInputStream(data);
                    return DefaultStreamedContent.builder()
                                .contentType("application/pdf")
                                .name(nomeFile.s)
                                .stream(() -> { return is; })
                                .build();                
                }
                else {
                    logger.info("Il documento richiesto  [{}] non è un PDF - tipologia [{}] id [{}] nome [{}].", nomeFileLocale, scope, id, nomeFile);
                    return getNoPDFDocument();
                }
            }
            catch(NoSuchFileException nsfe) {
                logger.error("Il file [{}] per la tipologia [{}] e id [{}] non è stato trovato su disco.", nomeFileLocale, scope, id);

                return getFileNotFoundDocument(scope, id);
            }
            catch (final IOException e) {            
                logger.error("Non riesco ad effettuare lo stream del documento [{}] tipologia [{}] e id [{}] a causa di {}", nomeFileLocale, scope, id, getStackTrace(e));

                return getErrorDocument();
            }
        }
    }
    
    private StreamedContent getErrorDocument() {
        try {
            final InputStream is = new ByteArrayInputStream(Files.readAllBytes(Paths.get(documentFolder+"/__error.pdf")));
            return DefaultStreamedContent.builder()
                            .contentType("application/pdf")
                            .name("error.pdf")
                            .stream(() -> { return is; })
                            .build();  
        }
        catch(IOException ioe) {
            logger.error("Non riesco ad effettuare lo stream del documento __error.pdf a causa di {}", getStackTrace(ioe));
            
            return new DefaultStreamedContent();
        }
    }

    private StreamedContent getNotFoundDocument(String scope, int id) {
        try {            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream pdfCs = new PDPageContentStream(doc, page)) {
                    pdfCs.beginText();
                    pdfCs.newLineAtOffset(100, 700);
                    pdfCs.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
                    pdfCs.showText("Non è stato possibile genereare il documento con codice ["+scope+"|"+id+"]. Contattare il supporto.");
                    pdfCs.endText();
                }                
                doc.save(baos);                
            }
            final InputStream is = new ByteArrayInputStream(baos.toByteArray());
            return DefaultStreamedContent.builder()
                            .contentType("application/pdf")
                            .name("not_found.pdf")
                            .stream(() -> { return is; })
                            .build();  
        }
        catch(IOException ioe) {
            logger.error("Non riesco ad effettuare lo stream del documento NOT_FOUND generato a causa di {}", getStackTrace(ioe));
            
            return new DefaultStreamedContent();
        }
    }
        
    private StreamedContent getFileNotFoundDocument(String scope, int id) {
        try {            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream pdfCs = new PDPageContentStream(doc, page)) {
                    pdfCs.beginText();
                    pdfCs.newLineAtOffset(100, 700);
                    pdfCs.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
                    pdfCs.showText("Il documento associato al codice ["+scope+"|"+id+"] non esiste su disco. Contattare il supporto.");
                    pdfCs.endText();
                }                
                doc.save(baos);                
            }
            final InputStream is = new ByteArrayInputStream(baos.toByteArray());
            return DefaultStreamedContent.builder()
                            .contentType("application/pdf")
                            .name("not_found.pdf")
                            .stream(() -> { return is; })
                            .build();  
        }
        catch(IOException ioe) {
            logger.error("Non riesco ad effettuare lo stream del documento NOT_FOUND generato a causa di {}", getStackTrace(ioe));
            
            return new DefaultStreamedContent();
        }
    }
    
    private StreamedContent getNoPDFDocument() {
        try {            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream pdfCs = new PDPageContentStream(doc, page)) {
                    pdfCs.beginText();
                    pdfCs.newLineAtOffset(100, 700);
                    pdfCs.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
                    pdfCs.showText("Il documento non è un file PDF visualizzabile. Scaricarlo e aprirlo con il programma adatto.");
                    pdfCs.endText();
                }                
                doc.save(baos);                
            }
            final InputStream is = new ByteArrayInputStream(baos.toByteArray());
            return DefaultStreamedContent.builder()
                            .contentType("application/pdf")
                            .name("empty.pdf")
                            .stream(() -> { return is; })
                            .build();  
        }
        catch(IOException ioe) {
            logger.error("Non riesco ad effettuare lo stream del documento NO_PDF generato a causa di {}", getStackTrace(ioe));
            
            return new DefaultStreamedContent();
        }
    }
    
    private StreamedContent getEmptyDocument() {
        try {            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream pdfCs = new PDPageContentStream(doc, page)) {
                    pdfCs.beginText();
                    pdfCs.newLineAtOffset(100, 700);
                    pdfCs.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
                    pdfCs.showText("Nessun documento selezionato.");
                    pdfCs.endText();
                }                
                doc.save(baos);                
            }
            final InputStream is = new ByteArrayInputStream(baos.toByteArray());
            return DefaultStreamedContent.builder()
                            .contentType("application/pdf")
                            .name("empty.pdf")
                            .stream(() -> { return is; })
                            .build();  
        }
        catch(IOException ioe) {
            logger.error("Non riesco ad effettuare lo stream del documento NO_PDF generato a causa di {}", getStackTrace(ioe));
            
            return new DefaultStreamedContent();
        }
    }
}
