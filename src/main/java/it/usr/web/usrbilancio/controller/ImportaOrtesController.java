/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import static it.usr.web.controller.BaseController.DATE_PATTERN_LONG;
import it.usr.web.producer.AppLogger;
import static it.usr.web.usrbilancio.controller.IvaController.extract;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.OrdinativoAppoggioService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.primefaces.model.file.UploadedFiles;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class ImportaOrtesController extends BaseController {
    private final static String CAUSALE_BENEFICIARIO = "Causale Beneficiario";
    private final static String IMPORTO_ACCREDITO = "Importo Accredito";
    private final static String FATTURA_PCC = "FATTURA PCC";
    private final static String DATA_EMISSIONE = "Data Emissione";
    private final static String DATA_DISPOSIZIONE = "Data Disposizione";
    private final static String ID_OPI = "ID OPI";
    private final static String DENOMINAZIONE = "Denominazione";
    private final static String NUMERO_FATTURA = "Numero Fattura";
    private final static String IMPORTO_FATTURA = "Importo Fattura";
    private final static String IMPORTO_QUOTA = "Importo Quota";
    private final static String[] DESCRIZIONE = {"Descrizione:", "Tipo movimento:"};  
    private final static String[] DATA_CONTABILE = {"Data contabile:", "Data di riferimento:"};
    private final static String[] IMPORTO = {"Importo:", "Data contabile:"};
    private final static String[] ID = {"Numero quietanza:", "Indicatore fruttifero"};
    private final static String[] ID_LONG = {"Numero quietanza:", "Indicatore fruttifero infruttifero:"};
    private final static String[] TIPOLOGIA = {"Tipologia:", "Ordinante:"};
    @Inject
    OrdinativoAppoggioService oas;
    @Inject
    CodiceService codServ;
    @Inject
    @AppLogger
    Logger logger;
    List<ImportaOrtesController.Result> result;
    UploadedFiles documenti;
    String gruppo;
    
    public void init() {
        result = null;
        documenti = null;
        gruppo = null;
    }

    public UploadedFiles getDocumenti() {
        return documenti;
    }

    public void setDocumenti(UploadedFiles documenti) {
        this.documenti = documenti;
    }

    public List<ImportaOrtesController.Result> getResult() {
        return result;
    }

    public String getGruppo() {
        return gruppo;
    }

    public void setGruppo(String gruppo) {
        this.gruppo = gruppo;
    }
       
    public void elabora() {
        result = null;

        if (documenti == null || isEmpty(documenti.getFiles())) {
            addMessage(Message.warn("Selezionare uno o più file prima di importare!"));
            return;
        }
        
        result = new ArrayList<>();
        AtomicInteger processati = new AtomicInteger(0);
        AtomicInteger errori = new AtomicInteger(0);
        List<OrdinativoAppoggioRecord> ordinativi = new ArrayList<>();
        documenti.getFiles().forEach(d -> {            
            try {
                processati.incrementAndGet();
                
                if(!d.getFileName().toLowerCase().endsWith("pdf")) {
                    result.add(new ImportaOrtesController.Result(true, d.getFileName(), "Il documento non è un ordinativo Or.Te.S.")); 
                    return;
                }
                                
                AtomicInteger nFatture = new AtomicInteger(0);
                List<OrdinativoAppoggioRecord> oar = elaboraFile(d.getContent(), nFatture);
                if(oar!=null) {
                    Documento doc = new Documento(d.getFileName(), gruppo, d.getContent(), d.getContentType());
                    oas.inserisci(oar, doc);
                    ordinativi.addAll(oar);
                    ImportaOrtesController.Result r = new ImportaOrtesController.Result(false, d.getFileName(), "File importato correttamente.");
                    if(nFatture.get()>0) r.setNote("Il documento contiene "+nFatture.get()+" fattura/e.");
                    result.add(r);
                }
                else {
                    oar = elaboraFileF24(d.getContent());
                    if(oar!=null) {
                        Documento doc = new Documento(d.getFileName(), gruppo, d.getContent(), d.getContentType());
                        oas.inserisci(oar, doc);
                        ImportaOrtesController.Result r = new ImportaOrtesController.Result(false, d.getFileName(), "File importato correttamente.");
                        r.setNote("Il documento è un modello F24-EP");
                        result.add(r);
                    }
                    else {
                        result.add(new ImportaOrtesController.Result(true, d.getFileName(), "Il documento non è un ordinativo Or.Te.S."));                    
                    }
                }
            } catch (IOException e) {
                errori.incrementAndGet();
                logger.error("Importazione del file [{}] non riuscita a causa di un errore inaspettato {}", d.getFileName(), getStackTrace(e));
                addMessage(Message.warn(e.getMessage()));
            }                        
        });
        
        addMessage(Message.info(processati.get()+" file processati, "+ordinativi.size()+" importati, "+errori.get()+" errori."));
    }
    
    private List<OrdinativoAppoggioRecord> elaboraFile(byte[] pdfFile, AtomicInteger nFatture) throws IOException {
        PDDocument doc = Loader.loadPDF(pdfFile);
        String txt = new PDFTextStripper().getText(doc);
        
        if(!txt.contains("ID OPI") && !txt.contains("ALIAS RGS")) return null;
        
        String[] lines = txt.split("\n");
        String idOpi = extract(lines, ID_OPI, String.class);                       
        String denominazione = extract(lines, DENOMINAZIONE, String.class);                        
        String causaleBeneficiario = extract(lines, CAUSALE_BENEFICIARIO, String.class);               
        LocalDate dataDisposizione = extract(lines, DATA_DISPOSIZIONE, LocalDate.class);                
        BigDecimal importoAccredito = extract(lines, IMPORTO_ACCREDITO, BigDecimal.class);        
        DatiDocumento dd = getDatiDocumento(causaleBeneficiario);
        List<OrdinativoAppoggioRecord> lApp = new ArrayList<>();
        LocalDate dataElaborazione = LocalDate.now();
        
        int numFatture = 0;        
        for (int numFatt = 1;; numFatt++) {
            boolean found = false;
            LocalDate dataEmissione = null;
            String numeroFattura = null;
            BigDecimal importoFattura = null;
            BigDecimal importoQuota = null;
            BigDecimal iva = null;
            
            for (int i = 0; i < lines.length && !found; i++) {
                String l = lines[i];
                if (l.contains(numFatt + FATTURA_PCC)) {
                    //int pcc = Integer.parseInt(removeText(l, FATTURA_PCC));                                        
                    for (int j = i + 1; j < lines.length; j++, i++) {
                        l = lines[j];
                        if (l.contains(DATA_EMISSIONE)) {
                            dataEmissione = LocalDate.parse(removeText(l, DATA_EMISSIONE), DATE_PATTERN_SHORT);                            
                        } else if (l.contains(NUMERO_FATTURA)) {
                            numeroFattura = removeText(l, NUMERO_FATTURA);
                        } else if (l.contains(IMPORTO_FATTURA)) {
                            importoFattura = new BigDecimal(removeText(l, IMPORTO_FATTURA));
                        } else if (l.contains(IMPORTO_QUOTA)) {
                            importoQuota = new BigDecimal(removeText(l, IMPORTO_QUOTA));
                            found = true;
                            break;
                        }
                    }

                    iva = importoFattura!=null ? importoFattura.subtract(importoQuota) : BigDecimal.ZERO;
                }
            }
            
            OrdinativoAppoggioRecord oar = new OrdinativoAppoggioRecord();
            oar.setBeneficiario(denominazione);
            oar.setDataPagamento(dataDisposizione);
            oar.setNumeroPagamento(idOpi);
            oar.setDataDocumento(dd.data);
            oar.setIdCodice(dd.idCodice);
            oar.setIdTipoRts(dd.idTipoRts);
            oar.setIdTipoDocumento(dd.idTipoDoc);
            oar.setNumeroDocumento(dd.numero!=null ? String.valueOf(dd.numero) : null);            
            oar.setDescrizioneRts(causaleBeneficiario); // verifica    
            oar.setProprietario(getUtente().getUsername());
            oar.setDataElaborazione(dataElaborazione);
            if(found) {
                oar.setImporto(importoQuota);                
                oar.setFatturaNumero(numeroFattura);
                oar.setFatturaData(dataEmissione);
                oar.setImportoIva(iva);
                lApp.add(oar);
                numFatture++;
            }
            else {
                if(numFatture==0) {
                    oar.setImporto(importoAccredito);
                    lApp.add(oar);
                }
                break;
            }
        }
        
        nFatture.set(numFatture);
        return lApp;
    }
    
    private List<OrdinativoAppoggioRecord> elaboraFileF24(byte[] content) throws IOException {
        PDDocument doc = Loader.loadPDF(content);
        String txt = new PDFTextStripper().getText(doc);

        if(!txt.contains("Rendicontazione Movimentazioni") && !txt.contains("Alias RGS:")) return null;
        
        String[] lines = txt.split("\n");
        for(int i=0;i<lines.length;i++) {
            lines[i] = lines[i].replace("\n", "").replace("\r", "");
        }
        
        String tiplogia = extractQ(lines, TIPOLOGIA, String.class);
        if(!"045".equals(tiplogia)) return null;
        String descrizione = extractQ(lines, DESCRIZIONE, String.class);        
        LocalDate dataContabile = extractQ(lines, DATA_CONTABILE, LocalDate.class);
        BigDecimal importo = extractQ(lines, IMPORTO, BigDecimal.class);
        //String id = extractQ(lines, ID, String.class, true).replace(" ", "");
        String id = extractQ(lines, ID, String.class); // aggiornamento documenti PDF da febbraio 2025
        if(id.length()>16) {
            id = extractQ(lines, ID_LONG, String.class);
            if(id.length()>16) throw new IOException("Impossibile estrarre il numero ordinativo/quietanza. Verificare lo zoom del documento.");
        }

        OrdinativoAppoggioRecord _ord = new OrdinativoAppoggioRecord();
        _ord.setImporto(importo);        
        _ord.setNumeroPagamento(id); 
        _ord.setDataPagamento(dataContabile);        
        _ord.setDescrizioneRts(descrizione);        
        _ord.setProprietario(getUtente().getUsername());
        _ord.setDataElaborazione(LocalDate.now());
        return Collections.singletonList(_ord);
    }
            
    private <T> T extract(final String[] lines, final String text, Class<T> type) {
        for(String l : lines) {
            if(l.contains(text)) {
                String data = removeText(l, text);
                switch(type.getName()) {
                    case "java.time.LocalDate" -> {
                        return type.cast(LocalDate.parse(data, DATE_PATTERN_SHORT));
                    }
                    case "java.math.BigDecimal" -> {
                        return type.cast(new BigDecimal(data));
                    }
                    case "java.lang.String" -> {
                        return type.cast(data);
                    }
                    case "java.lang.Integer" -> {
                        return type.cast(Integer.valueOf(data));
                    }
                }
            }
        }
        
        return null;
    }
    
    public static <T> T extractQ(String[] lines, String[] startEnd, Class<T> type) {
        return extractQ(lines, startEnd, type, false);
    }
    
    public static <T> T extractQ(String[] lines, String[] startEnd, Class<T> type, boolean last) {
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
    
    /**
     * 
     * @param descrCausale nel formato DE{C|T}NNNN-GGMMAA<blank>...
     * @return 
     */
    private DatiDocumento getDatiDocumento(String descrCausale) {
        DatiDocumento dd = new DatiDocumento();
        if(descrCausale!=null) {
            String[] parts = descrCausale.split(" ");
            if(parts.length>1) {
                parts = parts[0].split("\\-");
                if(parts.length>=2) {
                    if(parts[0].length()==7) {
                        TipoDocumentoRecord tdr = codServ.getTipoDocumentoByDescr(parts[0].substring(0, 3).toUpperCase());
                        dd.idTipoDoc = (tdr!=null) ? tdr.getId() : null;
                        try {
                            dd.numero = Integer.valueOf(parts[0].substring(3, 7));
                        }
                        catch(NumberFormatException nfe) {}
                                            
                        try {
                            dd.data = LocalDate.parse(parts[1], DateTimeFormatter.ofPattern("ddMMyy"));
                        }
                        catch(DateTimeParseException dtpe) {}
                    }   
                }
            }       
            
            parts = descrCausale.split("\\,");
            String cod = parts[parts.length-1].trim().replace(".", "").replace(" ", "");
            TipoRtsRecord trr = codServ.getTipoRtsByCodice(cod.substring(0, 1).toUpperCase());
            dd.idTipoRts = (trr!=null) ? trr.getId() : null;
            
            CodiceRecord cr = codServ.getCodiceByCodiceComposto(cod.substring(1));
            dd.idCodice = (cr!=null) ? cr.getId() : null;
        }
        
        return dd;
    }
    
    public String vediImportati() {        
        return Redirector.toPath("importati").withParam("d", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).withRedirect().go();                
    }
    
    public boolean allErrors(List<ImportaOrtesController.Result> res) {
        return (res==null) ? true : res.stream().allMatch(el -> el.error);
    }
        
    public static class DatiDocumento {
        public Integer idTipoDoc;
        public Integer numero;
        public LocalDate data;
        public Integer idTipoRts;
        public Integer idCodice;
        public String causale;
    }
    
    public static class Result {
        private boolean error;
        private String file;
        private String messaggio;
        private String note;

        public Result(boolean error, String file, String messaggio) {
            this(error, file, messaggio, null);
        }
                
        public Result(boolean error, String file, String messaggio, String note) {
            this.error = error;
            this.file = file;
            this.messaggio = messaggio;
            this.note = note;
        }

        public boolean isError() {
            return error;
        }

        public void setError(boolean error) {
            this.error = error;
        }
                
        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }
                
        public String getMessaggio() {
            return messaggio;
        }

        public void setMessaggio(String messaggio) {
            this.messaggio = messaggio;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }                
    }
}
