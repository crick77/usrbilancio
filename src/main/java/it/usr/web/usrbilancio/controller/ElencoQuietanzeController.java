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
import it.usr.web.usrbilancio.service.DuplicationException;
import it.usr.web.usrbilancio.service.QuietanzaService;
import it.usr.web.usrbilancio.service.StaleRecordException;
import jakarta.ejb.EJBException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.util.LangUtils;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class ElencoQuietanzeController extends BaseController {
    @Inject
    QuietanzaService qs;    
    @Inject
    CompetenzaService cs; 
    @Inject
    CodiceService codServ;
    @Inject
    @AppLogger
    Logger logger;    
    List<QuietanzaRecord> quietanze;   
    List<QuietanzaRecord> quietanzeFiltrate;
    List<CapitoloCompetenza> capComp;
    Map<Integer, CapitoloCompetenza> mCampComp;
    List<CodiceRecord> codici;
    Map<Integer, CodiceRecord> codiciMap;
    List<TipoRtsRecord> tipiRtsList;
    Map<Integer, TipoRtsRecord> tipiRts;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    String[] selectedCapitoli;
    List<String> capitoli;
    CapitoloCompetenza quietanzaCapComp;
    CodiceRecord quietanzaCodice;
    TipoRtsRecord quietanzaTipoRts;
    TipoDocumentoRecord quietanzaTipoDocumento;
    QuietanzaRecord quietanza;
    List<FilterMeta> filterBy;
    Integer quietanzaCodiceFiltro;
    BigDecimal totale;
    boolean dialogShow;
    UploadedFile documento;
    
    public void init() {        
        codici = codServ.getCodici();    
        codiciMap = new HashMap<>();
        codici.forEach(c -> codiciMap.put(c.getId(), c));
        tipiRtsList = codServ.getTipiRts(CodiceService.GruppoRts.RTS_QUIETANZA);
        tipiRts = new HashMap<>();
        tipiRtsList.forEach(t -> {
            tipiRts.put(t.getId(), t);
        });
        tipiDocumento = codServ.getTipiDocumentoAsMap();
        capComp = cs.getCapitoliCompetenze();  
        
        mCampComp = new HashMap<>();
        capComp.forEach(cc -> {
            mCampComp.put(cc.getId(), cc);
        });
        
        quietanzaCapComp = null;
        quietanzaCodice = null;
        quietanzaTipoDocumento = null;
        quietanzaTipoRts = null;     
        selectedCapitoli = null;
        quietanzaCodiceFiltro = null;
        
        filterBy = new ArrayList<>();
        
        aggiornaQuietanze();
        clearFilters(false);
    }

    public Integer getQuietanzaCodiceFiltro() {
        return quietanzaCodiceFiltro;
    }

    public void setQuietanzaCodiceFiltro(Integer quietanzaCodiceFiltro) {
        this.quietanzaCodiceFiltro = quietanzaCodiceFiltro;
    }
        
    public List<QuietanzaRecord> getQuietanze() {
        return quietanze;
    }

    public List<CapitoloCompetenza> getCapComp() {
        return capComp;
    }
        
    public List<QuietanzaRecord> getQuietanzeFiltrate() {
        return quietanzeFiltrate;
    }
        
    public void setQuietanzeFiltrate(List<QuietanzaRecord> quietanzeFiltrate) {
        this.quietanzeFiltrate = quietanzeFiltrate;
    }        

    public QuietanzaRecord getQuietanza() {
        return quietanza;
    }

    public String[] getSelectedCapitoli() {
        return selectedCapitoli;
    }

    public void setSelectedCapitoli(String[] selectedCapitoli) {
        this.selectedCapitoli = selectedCapitoli;
    }

    public List<String> getCapitoli() {
        return capitoli;
    }
        
    public void setQuietanza(QuietanzaRecord quietanza) {
        this.quietanza = quietanza;
        
        quietanzaCapComp = mCampComp.get(quietanza.getIdCompetenza());
        quietanzaCodice = codiciMap.get(quietanza.getIdCodice());
        quietanzaTipoDocumento = tipiDocumento.get(quietanza.getIdTipoDocumento());
        quietanzaTipoRts = tipiRts.get(quietanza.getIdTipoRts());
    }
            
    public CodiceRecord decodeCodice(int idCodice) {
        return codiciMap.get(idCodice);
    }
    
    public TipoRtsRecord decodeTipoRts(int idTipoRts) {
        return tipiRts.get(idTipoRts);
    }
    
    public TipoDocumentoRecord decodeTipoDocumento(int idTipoDocumento) {
        return tipiDocumento.get(idTipoDocumento);
    }
    
    public List<TipoDocumentoRecord> getTipiDocumentoList() {
        return new ArrayList<>(tipiDocumento.values());
    }
    
    public List<TipoRtsRecord> getTipiRtsList() {
        return tipiRtsList;
    } 
    
    public List<CodiceRecord> getCodici() {
        return codici;
    }

    public Map<Integer, TipoRtsRecord> getTipiRts() {
        return tipiRts;
    }

    public Map<Integer, TipoDocumentoRecord> getTipiDocumento() {
        return tipiDocumento;
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

    public BigDecimal getTotale() {
        return totale;
    }
            
    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }

    public boolean isDialogShow() {
        return dialogShow;
    }

    public void setDialogShow(boolean dialogShow) {
        this.dialogShow = dialogShow;
    }

    public UploadedFile getDocumento() {
        return documento;
    }

    public void setDocumento(UploadedFile documento) {
        this.documento = documento;
    }
                    
    public void aggiornaQuietanze() {
        quietanze = qs.getQuietanze();
        totale = BigDecimal.ZERO;
        if(!isEmpty(quietanze)) {
            quietanze.forEach(qf -> totale = totale.add(qf.getImporto()));
        }
        quietanzeFiltrate = null;
        quietanza = null;
        dialogShow = false;
        documento = null;
    }
    
    public void aggiornaTotale() {
        totale = BigDecimal.ZERO;
        if(!isEmpty(quietanzeFiltrate)) {
            quietanzeFiltrate.forEach(qf -> totale = totale.add(qf.getImporto()));
        }      
    }
    
    public boolean filtroGlobale(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (LangUtils.isBlank(filterText)) {
            return true;
        }
        
        QuietanzaRecord rec = (QuietanzaRecord)value;
        boolean match = contains(rec.getOrdinante(), filterText)
            || contains(rec.getDescrizioneOrdinanza(), filterText);
        if(isDate(filterText)) {
            LocalDate d = toDate(filterText);
            match = match || d.equals(rec.getDataDocumento())
                          || d.equals(rec.getDataPagamento());
        }
        match = match || contains(rec.getNumeroDocumento(), filterText);
        match = match || contains(rec.getNumeroPagamento(), filterText);
        match = match || contains(toStringFormat(rec.getImporto()), filterText);
        
        return match;
    }
    
    public CapitoloCompetenza decodeCapComp(int idCapComp) {
        return mCampComp.get(idCapComp);
    }
    
    public String dateFormat(LocalDate dt) {
        return (dt!=null) ? dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "";        
    }
    
    public boolean modificabile(QuietanzaRecord q) {
        if(q==null) return false;
        CapitoloCompetenza cc = mCampComp.get(q.getIdCompetenza());
        return (cc.getChiuso()!=1 && cc.getChiuso()!=2);
    }
    
    public boolean modificabile() {
        return modificabile(quietanza);        
    }
    
    public void salva() {
        if(!quietanza.changed() && documento==null) {
            aggiornaQuietanze();
            return;
        }
        
        quietanza.setIdCompetenza(quietanzaCapComp.getId());
        quietanza.setIdCodice(quietanzaCodice.getId());
        quietanza.setIdTipoRts(quietanzaTipoRts.getId());
        quietanza.setIdTipoDocumento(quietanzaTipoDocumento!=null ? quietanzaTipoDocumento.getId() : null);
        
        Documento doc = (documento!=null) ? new Documento(documento.getFileName(), null, documento.getContent(), codServ.getMimeType(documento.getContentType())) : null;
        
        try {
            qs.modifica(quietanza, doc);
            
            aggiornaQuietanze();            
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato aggiornato da un altro utente. Aggiornare e riprovare."));
            }
            else if(ex.getCausedByException() instanceof DuplicationException) {
                addMessage(Message.warn("Esiste già una quietanza con i dati indicati."));                
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il salvataggio della quietanza {}. Errrore: {}", ex.getCausedByException().getClass(), quietanza, ex.getCausedByException());
            }
        }          
    }
    
    public void elimina() {
        try {
            qs.elimina(quietanza);
            aggiornaQuietanze();
            addMessage(Message.info("Quitetanza eliminata correttamente."));
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato già eliminato da un altro utente. Aggiornare e riprovare."));
            }            
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione della quietanza {}. Errrore: {}", ex.getCausedByException().getClass(), quietanza, ex.getCausedByException());
            }
        }   
    }
    
    public boolean filterFunctionCapitoli(Object value, Object filter, Locale locale) {
        if(value==null || !(value instanceof String)) return true;
        String sVal = (String)value;
        if(selectedCapitoli==null || selectedCapitoli.length==0) return false;
        for(String sel : selectedCapitoli) {
            if(sel.equalsIgnoreCase(sVal)) return true;
        }
        
        return false;
    }
    
    public void clearFilters(boolean resetFilters) {
        capitoli = new ArrayList<>();
        capComp.forEach(cc -> {        
            capitoli.add(cc.getAnno()+" | "+cc.getDescrizione());
        });
        selectedCapitoli = new String[capitoli.size()];
        capitoli.toArray(selectedCapitoli);
        quietanzaCodiceFiltro = null;
        
        if(resetFilters) {
            PrimeFaces.current().executeScript("PF('quietanzeTable').clearFilters();PF('capcompcbm').checkAll()");
        }
    }
    
    public void duplica() {
        try {
            if(quietanza!=null) {
                QuietanzaRecord qq = quietanza.copy();
                qq.setId(null);
                qq.setNomefile("vuoto.pdf");
                qq.setNomefileLocale("__blank.pdf");
                qq.setVersione(1L);
                qs.inserisci(qq, null);
                
                aggiornaQuietanze();
                addMessage(Message.info("Quitetanza duplicata correttamente."));
            }
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato già eliminato da un altro utente. Aggiornare e riprovare."));
            }            
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante la dulicazione della quietanza {}. Errrore: {}", ex.getCausedByException().getClass(), quietanza, ex.getCausedByException());
            }
        } 
    }
}
