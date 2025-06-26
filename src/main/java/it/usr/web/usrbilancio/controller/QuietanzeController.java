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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.ejb.EJBException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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
public class QuietanzeController extends BaseController {
    @Inject
    QuietanzaService qs;
    @Inject
    CompetenzaService cs;
    @Inject
    CodiceService codServ;
    @Inject
    @AppLogger
    Logger logger;    
    boolean modifica;
    List<QuietanzaRecord> quietanze;
    QuietanzaRecord quietanza;
    CapitoloCompetenza quietanzaCapComp;
    CodiceRecord quietanzaCodice;
    TipoRtsRecord quietanzaTipoRts;
    TipoDocumentoRecord quietanzaTipoDocumento;
    List<CapitoloCompetenza> capComp;
    CapitoloCompetenza capCompSelezionato;
    Map<Integer, CapitoloCompetenza> mCampComp;
    Map<Integer, CodiceRecord> codici;
    List<CodiceRecord> codiciList;
    Map<Integer, TipoRtsRecord> tipiRts;
    Integer codice;
    List<TipoRtsRecord> tipiRtsList;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    UploadedFile documento;
    List<QuietanzaRecord> quietanzeFiltrate;
    BigDecimal totale;
    BigDecimal totaleParziale;
    List<FilterMeta> filterBy;
        
    public void init() {
        codiciList = codServ.getCodici();
        codici = new HashMap<>();
        codiciList.forEach(c -> codici.put(c.getId(), c));
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
        quietanze = null;
        quietanza = new QuietanzaRecord();
        quietanzaCapComp = null;
        quietanzaCodice = null;
        quietanzaTipoRts = null;
        quietanzaTipoDocumento = null;
        documento = null;
        modifica = false;
        capCompSelezionato = null;
        quietanzeFiltrate = null;
        codice = null;
        filterBy = new ArrayList<>();
    }

    public void reload() {
        quietanza = new QuietanzaRecord();
        quietanzaCapComp = null;
        quietanzaCodice = null;
        quietanzaTipoRts = null;
        quietanzaTipoDocumento = null;
        documento = null;
        modifica = false;
                
        aggiornaQuietanze();
    }
    
    public CapitoloCompetenza getCapCompSelezionato() {
        return capCompSelezionato;
    }

    public void setCapCompSelezionato(CapitoloCompetenza capCompSelezionato) {
        this.capCompSelezionato = capCompSelezionato;  
        
        modifica = true;
    }

    public List<CapitoloCompetenza> getCapComp() {
        return capComp;
    }

    public List<QuietanzaRecord> getQuietanze() {
        return quietanze;
    }

    public CodiceRecord decodeCodice(int idCodice) {
        return codici.get(idCodice);
    }
    
    public TipoRtsRecord decodeTipoRts(int idTipoRts) {
        return tipiRts.get(idTipoRts);
    }
            
    public TipoDocumentoRecord decodeTipoDocumento(int idTipoDocumento) {
        return tipiDocumento.get(idTipoDocumento);
    }
    
    public void aggiornaQuietanze() {
        quietanze = qs.getQuietanzeCompetenza(capCompSelezionato.getId());
        totale = BigDecimal.ZERO;
        quietanze.forEach(q -> {
            totale = totale.add(q.getImporto());
        });
        totaleParziale = totale;
        quietanzeFiltrate = null;
        quietanza = null;        
    }

    public void aggiornaParziale() {
        totaleParziale = BigDecimal.ZERO;
        if(quietanzeFiltrate!=null && !quietanzeFiltrate.isEmpty()) {
            quietanzeFiltrate.forEach(qf -> {
                totaleParziale = totaleParziale.add(qf.getImporto());
            });
        }            
    }
    
    public QuietanzaRecord getQuietanza() {
        return quietanza;
    }

    public void setQuietanza(QuietanzaRecord quietanza) {
        this.quietanza = quietanza;
        this.quietanza.reset();
        
        quietanzaCapComp = mCampComp.get(quietanza.getIdCompetenza());
        quietanzaCodice = codici.get(quietanza.getIdCodice());
        quietanzaTipoDocumento = tipiDocumento.get(quietanza.getIdTipoDocumento());
        quietanzaTipoRts = tipiRts.get(quietanza.getIdTipoRts());
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

    public Integer getCodice() {
        return codice;
    }

    public void setCodice(Integer codice) {
        this.codice = codice;
    }
        
    public Map<Integer, CodiceRecord> getCodici() {
        return codici;
    }

    public List<CodiceRecord> getCodiciList() {
        return codiciList;
    }
        
    public Map<Integer, TipoRtsRecord> getTipiRts() {
        return tipiRts;
    }

    public Map<Integer, TipoDocumentoRecord> getTipiDocumento() {
        return tipiDocumento;
    }

    public List<TipoDocumentoRecord> getTipiDocumentoList() {
        return new ArrayList<>(tipiDocumento.values());
    }
    
    public List<TipoRtsRecord> getTipiRtsList() {
        return tipiRtsList;
    }    
    
    public UploadedFile getDocumento() {
        return documento;
    }

    public void setDocumento(UploadedFile documento) {
        this.documento = documento;
    }

    public List<QuietanzaRecord> getQuietanzeFiltrate() {
        return quietanzeFiltrate;
    }

    public void setQuietanzeFiltrate(List<QuietanzaRecord> quietanzeFiltrate) {
        this.quietanzeFiltrate = quietanzeFiltrate;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public BigDecimal getTotaleParziale() {
        return totaleParziale;
    }

    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }
                    
    public boolean isModifica() {
        return modifica;
    }
                 
    public void nuova() {
        quietanza = new QuietanzaRecord();
        quietanza.setVersione(1L);
        quietanzaCapComp = capCompSelezionato;
        quietanzaCodice = null;
        quietanzaTipoRts = null;
        quietanzaTipoDocumento = null;
        documento = null;
        modifica = false;
    }
    
    public void salva() {
        if(!quietanza.changed() && documento==null) {
            reload();
            PrimeFaces.current().executeScript("PF('quietanzaDialog').hide();");
            return;
        }
        
        quietanza.setIdCompetenza(quietanzaCapComp.getId());
        quietanza.setIdCodice(quietanzaCodice.getId());
        quietanza.setIdTipoRts(quietanzaTipoRts.getId());
        quietanza.setIdTipoDocumento(quietanzaTipoDocumento!=null ? quietanzaTipoDocumento.getId() : null);
        
        Documento doc = (documento!=null) ? new Documento(documento.getFileName(), null, documento.getContent(), codServ.getMimeType(documento.getContentType())) : null;
        
        try {
            if(quietanza.getId()==null)
                qs.inserisci(quietanza, doc);
            else
                qs.modifica(quietanza, doc);
            
            reload();
            
            PrimeFaces.current().executeScript("PF('quietanzaDialog').hide();");
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
    
    public boolean isBloccato() {
        return (capCompSelezionato==null) ? true : (capCompSelezionato.getChiuso()==1 || capCompSelezionato.getChiuso()==2);
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
