/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.QuietanzaService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class ImportQOrtesController extends BaseController {
    private final static String[] CAUSALE = {"Causale beneficiario:", "Descrizione:"};
    private final static String[] ORDINANTE = {"Descrizione ordinante:", "Data disposizione:"};
    private final static String[] DATA_CONTABILE = {"Data contabile:", "Data di riferimento:"};
    private final static String[] IMPORTO = {"Importo:", "Data contabile:"};
    private final static String[] ID = {"Numero quietanza:", "Indicatore fruttifero"};
    private final static String[] ID_LONG = {"Numero quietanza:", "Indicatore fruttifero infruttifero:"};
    @Inject
    CompetenzaService cs;
    @Inject
    QuietanzaService qs;
    @Inject
    CodiceService codServ;
    @Inject
    @AppLogger
    Logger logger;  
    QuietanzaRecord quietanza;
    UploadedFile file;
    CapitoloCompetenza quietanzaCapComp;
    CodiceRecord quietanzaCodice;
    TipoRtsRecord quietanzaTipoRts;
    TipoDocumentoRecord quietanzaTipoDocumento;
    List<CapitoloCompetenza> capComp;
    CapitoloCompetenza capCompSelezionato;
    Map<Integer, CapitoloCompetenza> mCampComp;
    Map<Integer, CodiceRecord> codici;
    Map<Integer, TipoRtsRecord> tipiRts;
    List<TipoRtsRecord> tipiRtsList;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;    
    
    public void init() {
        codici = codServ.getCodiciAsMap();
        tipiRtsList = codServ.getTipiRts(CodiceService.GruppoRts.RTS_QUIETANZA);
        tipiRts = new HashMap<>();
        tipiRtsList.forEach(t -> tipiRts.put(t.getId(), t));        
        List<TipoDocumentoRecord> lTipoDoc = codServ.getTipiDocumentoNuovi();
        tipiDocumento = new HashMap<>();
        lTipoDoc.forEach(d -> tipiDocumento.put(d.getId(), d));
        capComp = cs.getCapitoliCompetenzeApertiNonFuturi();  
        mCampComp = new HashMap<>();
        capComp.forEach(cc -> mCampComp.put(cc.getId(), cc));
         
        quietanza = null;
        file = null;
    }

    public QuietanzaRecord getQuietanza() {
        return quietanza;
    }

    public void setQuietanza(QuietanzaRecord quietanza) {
        this.quietanza = quietanza;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public CapitoloCompetenza getQuietanzaCapComp() {
        return quietanzaCapComp;
    }

    public void setQuietanzaCapComp(CapitoloCompetenza quietanzaCapComp) {
        this.quietanzaCapComp = quietanzaCapComp;
    }

    public CodiceRecord getQuietanzaCodice() {
        return quietanzaCodice;
    }

    public void setQuietanzaCodice(CodiceRecord quietanzaCodice) {
        this.quietanzaCodice = quietanzaCodice;
    }

    public TipoRtsRecord getQuietanzaTipoRts() {
        return quietanzaTipoRts;
    }

    public void setQuietanzaTipoRts(TipoRtsRecord quietanzaTipoRts) {
        this.quietanzaTipoRts = quietanzaTipoRts;
    }

    public TipoDocumentoRecord getQuietanzaTipoDocumento() {
        return quietanzaTipoDocumento;
    }

    public void setQuietanzaTipoDocumento(TipoDocumentoRecord quietanzaTipoDocumento) {
        this.quietanzaTipoDocumento = quietanzaTipoDocumento;
    }

    public List<CapitoloCompetenza> getCapComp() {
        return capComp;
    }

    public void setCapComp(List<CapitoloCompetenza> capComp) {
        this.capComp = capComp;
    }

    public CapitoloCompetenza getCapCompSelezionato() {
        return capCompSelezionato;
    }

    public void setCapCompSelezionato(CapitoloCompetenza capCompSelezionato) {
        this.capCompSelezionato = capCompSelezionato;
    }

    public Map<Integer, CapitoloCompetenza> getmCampComp() {
        return mCampComp;
    }

    public void setmCampComp(Map<Integer, CapitoloCompetenza> mCampComp) {
        this.mCampComp = mCampComp;
    }

    public Map<Integer, CodiceRecord> getCodici() {
        return codici;
    }

    public void setCodici(Map<Integer, CodiceRecord> codici) {
        this.codici = codici;
    }

    public Map<Integer, TipoRtsRecord> getTipiRts() {
        return tipiRts;
    }

    public List<TipoRtsRecord> getTipiRtsList() {
        return tipiRtsList;
    }
    
    public void setTipiRts(Map<Integer, TipoRtsRecord> tipiRts) {
        this.tipiRts = tipiRts;
    }

    public Map<Integer, TipoDocumentoRecord> getTipiDocumento() {
        return tipiDocumento;
    }

    public void setTipiDocumento(Map<Integer, TipoDocumentoRecord> tipiDocumento) {
        this.tipiDocumento = tipiDocumento;
    }
    
    public void elabora() {
        try {
            quietanza = null;
            
            logger.info("Importazione quietanza dal file [{}] tipo [{}] dimensione [{}].", file.getFileName(), file.getContentType(), file.getSize());
            QuietanzaRecord qr = extractPDF(file.getContent());
            if(qr==null) {
                addMessage(Message.error("Il documento caricato NON è una quietanza Or.TeS."));
                return;
            }
            logger.info("Decodifica quietanza dal file [{}] -> {}", file.getFileName(), qr);
            
            quietanza = qr;
        }
        catch(IOException e) {
            logger.error("Impossibile elaborare il file {} a causa di {}", file.getFileName(), e.toString());
            addMessage(Message.error("Impossibile elaborare il file per un errore inatteso: "+e.toString()));
        }
    }
    
    public void salva() { 
        try {
            Documento doc = new Documento(file.getFileName(), null, file.getContent(), codServ.getMimeType(file.getContentType()));
            quietanza.setIdCodice(quietanzaCodice.getId());
            quietanza.setIdCompetenza(quietanzaCapComp.getId());
            quietanza.setIdTipoRts(quietanzaTipoRts.getId());            
            quietanza.setIdTipoDocumento(quietanzaTipoDocumento!=null ? quietanzaTipoDocumento.getId() : null);
            qs.inserisci(quietanza, doc);
            
            annulla();
            
            addMessage(Message.info("Quietanza salvata con successo."));
        }
        catch(Exception e) {
            logger.error("Impossibile salvare la quietanza a causa di {}", e.toString());
            addMessage(Message.error("Impossibile salvare la quietanza per un errore inatteso: "+e.toString()));
        }
    }            
    
    public void annulla() {
        file = null;
        quietanza = null;
        quietanzaCodice = null;
        quietanzaCapComp = null;
        quietanzaTipoDocumento = null;
        quietanzaTipoRts = null;
    }
    
    public QuietanzaRecord extractPDF(byte[] content) throws IOException {
        PDDocument doc = Loader.loadPDF(content);
        String txt = new PDFTextStripper().getText(doc);

        if(!txt.contains("Rendicontazione Movimentazioni") && !txt.contains("Alias RGS:")) return null;
        
        String[] lines = txt.split("\n");
        for(int i=0;i<lines.length;i++) {
            lines[i] = lines[i].replace("\n", "").replace("\r", "");
        }
        
        String causale = extract(lines, CAUSALE, String.class);
        String ordinante = extract(lines, ORDINANTE, String.class);
        LocalDate dataContabile = extract(lines, DATA_CONTABILE, LocalDate.class);
        BigDecimal importo = extract(lines, IMPORTO, BigDecimal.class);
        //String id = extract(lines, ID, String.class, true);
        String id = extract(lines, ID, String.class); // aggiornamento documenti PDF da febbraio 2025
        if(id.length()>16) {
            id = extract(lines, ID_LONG, String.class);
            if(id.length()>16) throw new IOException("Impossibile estrarre il numero ordinativo/quietanza. Verificare lo zoom del documento.");
        }

        QuietanzaRecord _quietanza = new QuietanzaRecord();
        _quietanza.setImporto(importo);
        //id = id.split("-")[3].trim()+"-"+id.split("-")[4].trim();
        _quietanza.setNumeroPagamento(id); 
        _quietanza.setDataPagamento(dataContabile);
        _quietanza.setOrdinante(ordinante);
        _quietanza.setDescrizioneOrdinanza(causale);
        if(notNull(ordinante).toUpperCase().startsWith("COM.STR.") || notNull(ordinante).toUpperCase().startsWith("COMMISSARIO")) {
            _quietanza.setFlag(1);
        }
        _quietanza.setVersione(1L);  
        return _quietanza;
    }
     
    public static <T> T extract(String[] lines, String[] startEnd, Class<T> type) {
        return extract(lines, startEnd, type, false);
    }
    
    public static <T> T extract(String[] lines, String[] startEnd, Class<T> type, boolean last) {
        List<String> lS = Arrays.asList(lines);
        int idxStart = last ? lS.lastIndexOf(startEnd[0]) : lS.indexOf(startEnd[0]);
        if (idxStart == -1) {
            return null;
        }
        int idxEnd = last ? lS.lastIndexOf(startEnd[1]) : lS.indexOf(startEnd[1]);
        if (idxEnd == -1) {
            idxEnd = lines.length;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = idxStart+1; i < idxEnd; i++) {
            sb.append(lines[i]).append(" ");
        }
        switch (type.getName()) {
            case "java.time.LocalDate" -> {
                return type.cast(LocalDate.parse(sb.toString().trim(), DATE_PATTERN_LONG));
            }
            case "java.math.BigDecimal" -> {
                String data = sb.toString().trim().replace("EUR", "").replace(".", "").replace(",", ".").trim();
                return type.cast(new BigDecimal(data));
            }
            case "java.lang.String" -> {
                return type.cast(sb.toString().trim());
            }
            case "java.lang.Integer" -> {
                return type.cast(Integer.valueOf(sb.toString().trim()));
            }
        }
        
        return null;
    }
}
