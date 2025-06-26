/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.pdfextract.model.ContoCorrenteBancario;
import it.usr.pdfextract.model.PDFOrdinativo;
import it.usr.pdfextract.model.RegolazionePagamentiInContoSospeso;
import it.usr.pdfextract.model.RiversamentoSuCS;
import it.usr.pdfextract.model.RiversamentoSuErario;
import it.usr.pdfextract.model.RiversamentoSuTU;
import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.ImportException;
import it.usr.web.usrbilancio.service.OrdinativoAppoggioService;
import it.usr.web.usrbilancio.service.PDFExtractor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class ImportaOrdinativiController extends BaseController {
    @Inject
    OrdinativoAppoggioService oas;
    @Inject
    CodiceService codServ;
    @Inject
    @AppLogger
    Logger logger;    
    @Inject
    PDFExtractor pe;    
    List<Result> result;
    UploadedFiles documenti;
    
    public void init() {        
        result = null;
        documenti = null;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public UploadedFiles getDocumenti() {
        return documenti;
    }

    public void setDocumenti(UploadedFiles documenti) {
        this.documenti = documenti;
    }
                  
    public void elabora() {                
        if(documenti==null || isEmpty(documenti.getFiles())) {
            addMessage(Message.warn("Selezionare uno o più file prima di importare!"));
            result = null;
            return;
        }
                
        result = new ArrayList<>();
        int processati = 0;
        int errori = 0;
        for(UploadedFile f : documenti.getFiles()) {
            try {
                PDFOrdinativo pdfO = pe.buildOrdinativo(f.getContent());

                if(pdfO==null) {                    
                    Result r = new Result(true, f.getFileName(), "File NON importato correttamente", "Il documento non è un ordinativo GeoCos.");
                    result.add(r);
                    errori++;
                    continue;
                }

                OrdinativoAppoggioRecord o = new OrdinativoAppoggioRecord();
                o.setNumeroPagamento(String.valueOf(pdfO.getNumeroOrdine()));
                o.setDataPagamento(toLocalDate(pdfO.getDataOrdine()));
                o.setImporto(pdfO.getImporto());
                o.setProprietario(getUtente().getUsername());
                o.setFatturaNumero(pdfO.getNumeroFattura());
                o.setFatturaData(toLocalDate(pdfO.getDataFattura()));
                if(pdfO.getImportoTotaleDocumento()!=null) {
                    BigDecimal iva = pdfO.getImportoTotaleDocumento().subtract(pdfO.getImporto());
                    o.setImportoIva(iva);
                }
                o.setDataElaborazione(LocalDate.now());
                Documento doc = new Documento(f.getFileName(), null, f.getContent(), codServ.getMimeType(f.getContentType()));
                
                switch(pdfO.getModalitaEstinzione()) {
                    case "56" ->  {
                        ContoCorrenteBancario est = (ContoCorrenteBancario)pdfO.getEstinzione();
                        String ben = !isEmpty(est.getNomeECognomeDelBeneficiario()) ? est.getNomeECognomeDelBeneficiario() : est.getRagioneSocialeDelBeneficiario();
                        o.setBeneficiario(ben);
                        String desc = est.getDescrizioneCausale()!=null ? est.getDescrizioneCausale().split(",")[0] : null;
                        o.setDescrizioneRts(desc);
                        DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                        o.setIdCodice(dd.idCodice);
                        o.setIdTipoRts(dd.idTipoRts);
                        o.setIdTipoDocumento(dd.idTipoDoc);
                        o.setNumeroDocumento(dd.numero!=null ? String.valueOf(dd.numero) : null);
                        o.setDataDocumento(dd.data);    
                    }
                    case "77" ->  {
                        RiversamentoSuTU est = (RiversamentoSuTU)pdfO.getEstinzione();                    
                        o.setBeneficiario(est.getRagioneSocialeDelBeneficiario());
                        String desc = est.getDescrizioneCausale()!=null ? est.getDescrizioneCausale().split(",")[0] : null;
                        o.setDescrizioneRts(desc);
                        DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                        o.setIdCodice(dd.idCodice);
                        o.setIdTipoRts(dd.idTipoRts);
                        o.setIdTipoDocumento(dd.idTipoDoc);
                        o.setNumeroDocumento(dd.numero!=null ? String.valueOf(dd.numero) : null);
                        o.setDataDocumento(dd.data);
                    }
                    case "71" ->  {
                        RiversamentoSuErario est = (RiversamentoSuErario)pdfO.getEstinzione();
                        Integer capitolo = est.getCapitolo();
                        Integer articolo = est.getArticolo();
                        Integer capo = est.getCapo();
                        StringBuilder sb = new StringBuilder("BILANCIO CONTO ENTRATA");
                        if(capo!=null) sb.append(" CAPO ").append(capo);
                        if(capitolo!=null) sb.append(" CAPITOLO ").append(capitolo);
                        if(articolo!=null) sb.append(" ARTICOLO ").append(articolo);
                        o.setBeneficiario(sb.toString());
                        String desc = est.getDescrizioneCausale()!=null ? est.getDescrizioneCausale().split(",")[0] : null;
                        o.setDescrizioneRts(desc);
                        DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                        o.setIdCodice(dd.idCodice);
                        o.setIdTipoRts(dd.idTipoRts);
                        o.setIdTipoDocumento(dd.idTipoDoc);
                        o.setNumeroDocumento(dd.numero!=null ? String.valueOf(dd.numero) : null);
                        o.setDataDocumento(dd.data);
                    }
                    case "81" ->  {
                        RegolazionePagamentiInContoSospeso est = (RegolazionePagamentiInContoSospeso)pdfO.getEstinzione();
                        o.setBeneficiario(est.getRagioneSocialeDelBeneficiario());
                        String desc = est.getDescrizioneCausale()!=null ? est.getDescrizioneCausale().split(",")[0] : null;
                        o.setDescrizioneRts(desc);
                        DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                        o.setIdCodice(dd.idCodice);
                        o.setIdTipoRts(dd.idTipoRts);
                        o.setIdTipoDocumento(dd.idTipoDoc);
                        o.setNumeroDocumento(dd.numero!=null ? String.valueOf(dd.numero) : null);
                        o.setDataDocumento(dd.data);
                    }
                    case "76" ->  {
                        RiversamentoSuCS est = (RiversamentoSuCS)pdfO.getEstinzione();
                        o.setBeneficiario(est.getRagioneSocialeDelBeneficiario());
                        String desc = est.getDescrizioneCausale()!=null ? est.getDescrizioneCausale().split(",")[0] : null;
                        o.setDescrizioneRts(desc);
                        DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                        o.setIdCodice(dd.idCodice);
                        o.setIdTipoRts(dd.idTipoRts);
                        o.setIdTipoDocumento(dd.idTipoDoc);
                        o.setNumeroDocumento(dd.numero!=null ? String.valueOf(dd.numero) : null);
                        o.setDataDocumento(dd.data);
                    }
                }
                
                oas.inserisci(o, Collections.singletonList(doc));
                
                Result r = new Result(false, f.getFileName(), "File importato correttamente.");
                if(pdfO.getPagine()>2) {
                    r.setNote(" ATTENZIONE: Il documento si compone di più di 2 pagine e potrebbe contenere più di una fattura!");
                    errori++;
                }
                result.add(r);
                
                processati++;
            }
            catch(ImportException ie) {
                addMessage(Message.error("File "+f.getFileName()+", errore: "+ie));
                logger.error("Importazione del file [{}] non riuscita a causa di {}", f.getFileName(), ie.getMessage());
                Result r = new Result(true, f.getFileName(), "File NON importato correttamente.", ie.getMessage());
                result.add(r);
                errori++;
            }
            catch(Exception e) {                        
                addMessage(Message.error("File "+f.getFileName()+", errore: "+e));
                logger.error("Importazione del file [{}] non riuscita a causa di un errore inaspettato {}", f.getFileName(), getStackTrace(e));
                Result r = new Result(true, f.getFileName(), "File NON importato correttamente.", e.toString());
                result.add(r);
                errori++;
            }  
        }
        
        if(errori>0) {
            addMessage(Message.warn("Procedura di importazione completata con errori/notifiche. File processati: "+processati+", errori/notifiche: "+errori));
        }
        else {
            addMessage(Message.info("Procedura di importazione completata con successo! File processati: "+processati));
        }
    }
    
    public CodiceRecord getCodiceDescrizione(String descrCausale) {
        if(descrCausale!=null && descrCausale.contains(",")) {
            String[] parts = descrCausale.split("\\,");
            String cod = parts[parts.length-1].trim().replace(".", "").replace(" ", "");
            return codServ.getCodiceByCodiceComposto(cod);
        }
        
        return null;
    }
    
    public TipoDocumentoRecord getTipoDocumentoDescrizione(String descrCausale) {
        if(descrCausale!=null) {
            String[] parts = descrCausale.split("\\.");
            if(parts.length>1) {
                return codServ.getTipoDocumentoByDescr(parts[0]);
            }
            
        }
        
        return null;
    }
    
    public Integer getNumeroDocumentoDescrizione(String descrCausale) {
        if(descrCausale!=null) {
            String[] parts = descrCausale.split("\\.");
            if(parts.length>1) {
                parts = parts[1].split("\\-");
                try {
                    return Integer.valueOf(parts[0]);
                }
                catch(NumberFormatException nfe) {}
            }
            
        }
        
        return null;
    }
    
    public LocalDate getDataDocumentoDescrizione(String descrCausale) {
        if(descrCausale!=null) {
            String[] parts = descrCausale.split("\\.");
            if(parts.length>1) {
                parts = parts[1].split("\\-");
                if(parts.length>1) {
                    parts = parts[1].split(" ");
                    if(parts.length>1) {
                        try {
                            return LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyyMMdd"));
                        }
                        catch(DateTimeParseException dtpe) {}
                        
                        try {
                            return LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("ddMMyyyy"));
                        }
                        catch(DateTimeParseException dtpe) {}
                    }
                }
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
    
    public boolean allErrors(List<Result> res) {
        return (res==null) ? true : res.stream().allMatch(el -> el.error);
    }
    
    public String vediImportati() {        
        return Redirector.toPath("importati").withParam("d", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).withRedirect().go();                
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
