/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package it.usr.web.usrbilancio.servlet;

import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoCodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoRecord;
import it.usr.web.usrbilancio.domain.tables.records.MovimentiVirtualiRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.producer.DocumentFolder;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.MovimentiVirtualiService;
import it.usr.web.usrbilancio.service.OrdinativoAppoggioService;
import it.usr.web.usrbilancio.service.OrdinativoService;
import it.usr.web.usrbilancio.service.QuietanzaService;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import jakarta.inject.Inject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@WebServlet(name = "DownloadServlet", urlPatterns = {"/download"})
public class DownloadServlet extends HttpServlet {
    @Inject
    QuietanzaService qs;
    @Inject
    OrdinativoService os;
    @Inject
    OrdinativoAppoggioService oas;
    @Inject
    MovimentiVirtualiService mvs;
    @Inject
    CodiceService cs;
    @DocumentFolder
    @Inject
    String documentFolder;
    @AppLogger
    @Inject
    Logger logger;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String scope = request.getParameter("scope");
        scope = (scope != null) ? scope.toUpperCase() : "";
        String sId = request.getParameter("id");
        if (isEmpty(scope) || isEmpty(sId)) {
            streamData(response, "nontrovato.pdf", getNotFoundDocument(scope, sId));
            return;
        }
        
        int id = 0;
        int[] idMulti;
        if(isNumeric(sId)) {
            id = Integer.parseInt(sId);
            idMulti = new int[1];
            idMulti[0] = id;
        }
        else {
            String[] idSeq = sId.split(",");
            idMulti = new int[idSeq.length];
            for(int i=0;i<idSeq.length;i++) {
                if(isNumeric(idSeq[i])) {
                    idMulti[i] = Integer.parseInt(idSeq[i]);
                }
                else {
                    streamData(response, "nontrovato.pdf", getNotFoundDocument(scope, sId));
                    return;
                }
            }
        }

        String nomeFile;
        String nomeFileLocale = null;
        try {
            switch (scope) {
                case "Q": {
                    QuietanzaRecord q = qs.getQuietanzaById(id);
                    nomeFile = q.getNomefile();
                    nomeFileLocale = q.getNomefileLocale();
                    break;
                }
                case "O": {
                    AllegatoRecord a = os.getAllegatoById(id); 
                    nomeFile = a.getNomefile();
                    nomeFileLocale = a.getNomefileLocale();
                    break;
                }
                case "OA": {
                    AllegatoAppoggioRecord a = oas.getAllegatoAppoggioById(id);
                    nomeFile = a.getNomefile();
                    nomeFileLocale = a.getNomefileLocale();
                    break;
                }
                case "MV": {
                    MovimentiVirtualiRecord mv = mvs.getMovimentoVirtualeById(id);
                    nomeFile = mv.getNomefile();
                    nomeFileLocale = mv.getNomefileLocale();
                    break;
                }
                case "C": {
                    AllegatoCodiceRecord ac = cs.getAllegatoCodiceById(id);
                    nomeFile = ac.getNomefile();
                    nomeFileLocale = ac.getNomefileLocale();
                    break;
                }
                case "OZ": {
                    List<String> dupCheck = new ArrayList<>();
                    List<AllegatoRecord> all = new ArrayList<>();
                    for(int _id : idMulti) {
                        all.add(os.getAllegatoById(_id));
                    }      
                    
                    for(AllegatoRecord a : all) {
                        if(dupCheck.contains(a.getNomefile())) {
                            logger.error("Nella creazione del file zip il documento con tipologia [{}], id=[{}] e nome=[{}] risulta duplicato.", scope, id, a.getNomefile());
                            streamData(response, "dupfile.pdf", getDuplicateZipDocument(scope, id, a.getNomefile()));
                            return;
                        }
                        
                        dupCheck.add(a.getNomefile());
                    }
                    
                    try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        try(ZipOutputStream zos = new ZipOutputStream(baos)) {
                            for(AllegatoRecord a : all) {
                                logger.info("Creazione archvivio allegati ordinativo ID=[{}], File=[{}]...", id, a.getNomefile());
                                ZipEntry entry = new ZipEntry(a.getNomefile());
                                 
                                zos.putNextEntry(entry);
                                zos.write(Files.readAllBytes(Paths.get(documentFolder + "/" + a.getNomefileLocale())));
                                zos.closeEntry();                      
                            }  
                        }
                        streamData(response, "allegati_ordinativo.zip", baos.toByteArray());
                        return;
                    }                    
                }
                default: {
                    logger.error("Non riesco a caricare il documento con tipologia [{}] e id=[{}].", scope, id);
                    streamData(response, "errore.pdf", Files.readAllBytes(Paths.get(documentFolder + "/__error.pdf")));
                    return;
                }
            } // switch

            logger.info("Generazione stream documento [{}] tipologia [{}] id [{}] nome [{}].", nomeFileLocale, scope, id, nomeFile);
            streamData(response, nomeFile, Files.readAllBytes(Paths.get(documentFolder + "/" + nomeFileLocale)));
        } catch (NullPointerException npe) {
            logger.error("Non riesco a preparate il documento di tipologia [{}] e id [{}] perchè non trovato. Invio documento di notifica.", scope, id);
            streamData(response, "nontrovato.pdf", getNotFoundDocument(scope, id));

        } catch (NoSuchFileException nsfe) {
            logger.error("Il file [{}] per la tipologia [{}] e id [{}] non è stato trovato su disco.", nomeFileLocale, scope, id);
            streamData(response, "filenontrovato.pdf", getFileNotFoundDocument(scope, id));
        } catch (IOException e) {
            logger.error("Non riesco a preparare il documento di tipologia [{}] e id [{}] a causa di {}", scope, id, e.toString());
            streamData(response, "errore.pdf", Files.readAllBytes(Paths.get(documentFolder + "/__error.pdf")));
        }
    }

    private void streamData(HttpServletResponse response, String nomeFile, byte[] data) throws IOException {
        response.addHeader("content-disposition", "attachment;filename=" + nomeFile);
        response.setContentType("application/octet-stream;charset=UTF-8");
        try (OutputStream out = response.getOutputStream()) {
            out.write(data);
            out.flush();
        }
    }

    private byte[] getNotFoundDocument(String scope, Object id) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream pdfCs = new PDPageContentStream(doc, page)) {
                pdfCs.beginText();
                pdfCs.newLineAtOffset(100, 700);
                pdfCs.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
                pdfCs.showText("Non è stato possibile genereare il documento con codice [" + scope + "|" + id + "]. Contattare il supporto.");
                pdfCs.endText();
            }
            doc.save(baos);

            return baos.toByteArray();
        }
    }

    private byte[] getFileNotFoundDocument(String scope, int id) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream pdfCs = new PDPageContentStream(doc, page)) {
                pdfCs.beginText();
                pdfCs.newLineAtOffset(100, 700);
                pdfCs.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
                pdfCs.showText("Il documento associato al codice [" + scope + "|" + id + "] non esiste su disco. Contattare il supporto.");
                pdfCs.endText();
            }
            doc.save(baos);        
            
            return baos.toByteArray();
        }
    }

    private byte[] getDuplicateZipDocument(String scope, int id, String nomeFile) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream pdfCs = new PDPageContentStream(doc, page)) {
                pdfCs.beginText();
                pdfCs.newLineAtOffset(100, 700);
                pdfCs.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12);
                pdfCs.showText("L'allegato [" + nomeFile + "] associato al codice [" + scope + "|" + id + "] risulta duplicato. Eliminarne uno e riprovare oppure contattare il supporto.");
                pdfCs.endText();
            }
            doc.save(baos);        
            
            return baos.toByteArray();
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public boolean isEmpty(String s) {
        return (s == null) || (s.length() == 0);
    }

    public boolean isNumeric(String s) {
        try {
            Integer.valueOf(s);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        logger.info("{}: inizializzato. Percorso dei file [{}].", getServletName(), documentFolder);
    }
}
