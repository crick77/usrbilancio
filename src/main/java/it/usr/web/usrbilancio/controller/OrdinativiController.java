/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoRecord;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.DocumentService;
import it.usr.web.usrbilancio.service.DuplicationException;
import it.usr.web.usrbilancio.service.OrdinativoService;
import it.usr.web.usrbilancio.service.StaleRecordException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import jakarta.ejb.EJBException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.StringJoiner;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.primefaces.util.LangUtils;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class OrdinativiController extends BaseController {
    @Inject
    OrdinativoService os;
    @Inject
    CompetenzaService cs;
    @Inject
    CodiceService codServ;
    @Inject
    DocumentService ds;
    @Inject
    @AppLogger
    Logger logger;    
    List<OrdinativoRecord> ordinativi;
    List<OrdinativoRecord> ordinativiFiltrati; 
    List<OrdinativoRecord> ordinativiIva;
    List<AllegatoRecord> allegati;
    List<AllegatoRecord> allegatiSelezionati;
    OrdinativoRecord ordinativo;
    OrdinativoRecord ordinativoIva;
    AllegatoRecord allegato;
    CapitoloCompetenza ordinativoCapComp;    
    CodiceRecord ordinativoCodice;
    Integer ordinativoCodiceFiltro;
    TipoRtsRecord ordinativoTipoRts;
    TipoDocumentoRecord ordinativoTipoDocumento;
    List<CapitoloCompetenza> capComp;
    CapitoloCompetenza capCompSelezionato;
    CapitoloCompetenza capCompIvaSelezionato;
    Map<Integer, CapitoloCompetenza> mCampComp;
    List<CodiceRecord> codici;
    Map<Integer, CodiceRecord> codiciMap;
    Map<Integer, TipoRtsRecord> tipiRts;
    List<TipoRtsRecord> tipiRtsList;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;  
    List<TipoDocumentoRecord> tipiDocumentoList;
    UploadedFiles documentFiles;
    BigDecimal totale;
    BigDecimal totaleParziale;
    String azione;
    String gruppo;
    
    public void init() {
        codici = codServ.getCodici();
        codiciMap = new HashMap<>();
        codici.forEach(c -> codiciMap.put(c.getId(), c));
        
        tipiRtsList = codServ.getTipiRts(CodiceService.GruppoRts.RTS_ORDINATIVO);
        tipiRts = new HashMap<>();
        tipiRtsList.forEach(t -> tipiRts.put(t.getId(), t));
        tipiDocumento = codServ.getTipiDocumentoAsMap();
        tipiDocumentoList = new ArrayList<>(tipiDocumento.values());
        capComp = cs.getCapitoliCompetenze();  
        mCampComp = new HashMap<>();
        capComp.forEach(cc -> {
            mCampComp.put(cc.getId(), cc);
        });
        
        ordinativi = null;
        ordinativoIva = null;
        ordinativoCapComp = null;
        ordinativoCodice = null;
        ordinativoCodiceFiltro = null;
        ordinativoTipoRts = null;
        ordinativoTipoDocumento = null;
        capCompSelezionato = null;
        capCompIvaSelezionato = null;
        allegati = null;
        allegato = null;
        allegatiSelezionati = null;
        documentFiles = null;
        ordinativiFiltrati = null;
        ordinativiIva = null;   
        gruppo = null; 
    }

    public String getElencoDownloadAllegati() {
        if(isEmpty(allegatiSelezionati)) {
            return null;
        }
        else {
            StringJoiner sj = new StringJoiner(",", "", "");
            allegatiSelezionati.forEach(a -> sj.add(String.valueOf(a.getId())));
            return sj.toString();
        }
    }
    
    public List<AllegatoRecord> getAllegatiSelezionati() {
        return allegatiSelezionati;
    }

    public void setAllegatiSelezionati(List<AllegatoRecord> allegatiSelezionati) {
        this.allegatiSelezionati = allegatiSelezionati;
    }
           
    public List<OrdinativoRecord> getOrdinativi() {
        return ordinativi;
    }

    public void setOrdinativi(List<OrdinativoRecord> ordinativi) {
        this.ordinativi = ordinativi;
    }

    public OrdinativoRecord getOrdinativo() {
        return ordinativo;
    }

    public void setOrdinativo(OrdinativoRecord ordinativo) {
        this.ordinativo = ordinativo;
        
        if(ordinativo!=null) {
            this.ordinativoCapComp = mCampComp.get(ordinativo.getIdCompetenza());
            this.ordinativoCodice = codiciMap.get(ordinativo.getIdCodice());
            this.ordinativoTipoDocumento = tipiDocumento.get(ordinativo.getIdTipoDocumento());
            this.ordinativoTipoRts = tipiRts.get(ordinativo.getIdTipoRts());
            if(ordinativo.getOrdinativoIva()!=null) {                
                OrdinativoRecord o = os.getOrdinativoById(ordinativo.getOrdinativoIva());                
                this.capCompIvaSelezionato = cs.getCapitoloCompetenzaById(o.getIdCompetenza());
                aggiornaIvaOrdinativi();
                this.ordinativoIva = o;
            }
            else {
                this.ordinativoIva = null;
                this.capCompIvaSelezionato = null;
            }
            azione = "Modifica";
        }
    }

    public boolean isOrdinativoFlag() {
        return ordinativo!=null ? ((ordinativo.getFlag() & 1) == 1) : false;
    } 
 
    public void setOrdinativoFlag(boolean ordinativoFlag) {
        int currentFlag = ordinativo.getFlag();
        currentFlag = ordinativoFlag ? (currentFlag | 1) : (currentFlag & ~1);
        ordinativo.setFlag(currentFlag);
    }
    
    public CapitoloCompetenza getOrdinativoCapComp() {
        return ordinativoCapComp;
    }

    public void setOrdinativoCapComp(CapitoloCompetenza ordinativoCapComp) {
        this.ordinativoCapComp = ordinativoCapComp;
    }

    public Integer getOrdinativoCodiceFiltro() {
        return ordinativoCodiceFiltro;
    }

    public void setOrdinativoCodiceFiltro(Integer ordinativoCodiceFiltro) {
        this.ordinativoCodiceFiltro = ordinativoCodiceFiltro;
    }

    public CodiceRecord getOrdinativoCodice() {
        return ordinativoCodice;
    }

    public void setOrdinativoCodice(CodiceRecord ordinativoCodice) {
        this.ordinativoCodice = ordinativoCodice;
    }
        
    public TipoRtsRecord getOrdinativoTipoRts() {
        return ordinativoTipoRts;
    }

    public void setOrdinativoTipoRts(TipoRtsRecord ordinativoTipoRts) {
        this.ordinativoTipoRts = ordinativoTipoRts;
    }

    public TipoDocumentoRecord getOrdinativoTipoDocumento() {
        return ordinativoTipoDocumento;
    }

    public void setOrdinativoTipoDocumento(TipoDocumentoRecord ordinativoTipoDocumento) {
        this.ordinativoTipoDocumento = ordinativoTipoDocumento;
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

    public List<CodiceRecord> getCodici() {
        return codici;
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

    public List<TipoDocumentoRecord> getTipiDocumentoList() {
        return tipiDocumentoList;
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

    public List<AllegatoRecord> getAllegati() {
        return allegati;
    }

    public void setAllegati(List<AllegatoRecord> allegati) {
        this.allegati = allegati;
    }

    public AllegatoRecord getAllegato() {
        return allegato;
    }

    public void setAllegato(AllegatoRecord allegato) {
        this.allegato = allegato;
    }
    
    public UploadedFiles getDocumentFiles() {
        return documentFiles;
    }

    public void setDocumentFiles(UploadedFiles documentFiles) {
        this.documentFiles = documentFiles;
    }

    public List<OrdinativoRecord> getOrdinativiFiltrati() {
        return ordinativiFiltrati;
    }

    public void setOrdinativiFiltrati(List<OrdinativoRecord> ordinativiFiltrati) {
        this.ordinativiFiltrati = ordinativiFiltrati;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public BigDecimal getTotaleParziale() {
        return totaleParziale;
    }

    public CapitoloCompetenza getCapCompIvaSelezionato() {
        return capCompIvaSelezionato;
    }

    public void setCapCompIvaSelezionato(CapitoloCompetenza capCompIvaSelezionato) {
        this.capCompIvaSelezionato = capCompIvaSelezionato;
    }

    public List<OrdinativoRecord> getOrdinativiIva() {
        return ordinativiIva;
    }

    public OrdinativoRecord getOrdinativoIva() {
        return ordinativoIva;
    }

    public void setOrdinativoIva(OrdinativoRecord ordinativoIva) {
        this.ordinativoIva = ordinativoIva;
    }

    public String getAzione() {
        return azione;
    }

    public String getGruppo() {
        return gruppo;
    } 

    public void setGruppo(String gruppo) {
        this.gruppo = gruppo;
    }
                    
    public void aggiornaOrdinativi() {
        ordinativi = os.getOrdinativiByCompetenza(capCompSelezionato.getId());
        totale = BigDecimal.ZERO;
        ordinativi.forEach(o -> {
            totale = totale.add(o.getImporto());
        });
        totaleParziale = totale;
        ordinativiFiltrati = null;
        ordinativo = null;
        allegati = null;
        allegato = null;
        ordinativoIva = null;
        azione = null;
        gruppo = null;
        PrimeFaces.current().executeScript("PF('ordinativiTable').clearFilters();");
    }
            
    public void aggiornaIvaOrdinativi() {
        ordinativiIva = os.getOrdinativiByCompetenza(capCompIvaSelezionato.getId());
        for(int i=0;i<ordinativiIva.size();i++) {
            if(Objects.equals(ordinativiIva.get(i).getId(), ordinativo.getId())) {
                ordinativiIva.remove(i);
                break;
            }                    
        }
        ordinativoIva = null;
    }
    
    public void aggiornaParziale() {
        totaleParziale = BigDecimal.ZERO;
        if(!isEmpty(ordinativiFiltrati)) {
            ordinativiFiltrati.forEach(of -> {
                totaleParziale = totaleParziale.add(of.getImporto());
            });
        }      
        
        pulisciSelezione();
    }
    
    public void aggiornaAllegati() {
        allegati = os.getAllegatiOrdinativo(ordinativo.getId());
        allegato = null;
        allegatiSelezionati = null;
        PrimeFaces.current().executeScript("PF('documentiDialog').show()");
    }
    
    public String getUrl() {
        return (allegato!=null) ? "/DocumentServlet?scope=O&id="+allegato.getId() : null;
    }
    
    public void pulisciAllegato() {
        allegato = null;
    }
    
    public void nuovo() {
        ordinativo = new OrdinativoRecord();
        ordinativo.setRtsCompleto((byte)0);
        ordinativo.setRtsStampato((byte)0);
        ordinativo.setVersione(1L);
        
        ordinativoCapComp = capCompSelezionato;
        ordinativoCodice = null;
        ordinativoTipoDocumento = null;
        ordinativoTipoRts = null;
        azione = "Nuovo";
        gruppo = null;
    }
    
    public void aggiungiAllegati() {
        List<Documento> lAll = new ArrayList<>();
        for(UploadedFile f : documentFiles.getFiles()) {
            Documento d = new Documento(f.getFileName(), gruppo, f.getContent(), codServ.getMimeType(f.getContentType()));
            lAll.add(d);
            
            // Se P7M sbusta e aggiunge
            if(d.getFileName().toLowerCase().endsWith("p7m")) {
                try {
                    Documento extr = ds.extractP7M(d);
                    if(extr!=null) {
                        lAll.add(extr);
                    }
                    else {
                        logger.debug("L'estrazione del file P7M {} non è andata a buon fine.", d.getFileName());
                    }
                }
                catch(Exception ex) {
                    logger.debug("Errore imprevisto {} durante l'estrazione di un file P7M per l'ordinativo {}. Errrore: {}", ex.getCause().getClass(), ordinativo, ex.getCause());        
                }
            }
        }
                        
        try {
            os.inserisci(lAll, ordinativo.getId());
            
            aggiornaAllegati();
            PrimeFaces.current().executeScript("PF('allegatiDialog').hide()");
        }
        catch(EJBException ex) {
            addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
            logger.debug("Errore imprevisto {} durante il salvataggio degli allegati dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());        
        }  
    }   
    
    public void modificaAllegato() {
        try {
            if(allegato.changed()) {
                os.modifica(allegato);
                aggiornaAllegati();                
            }
            
            PrimeFaces.current().executeScript("PF('descrizioneDocumentoDialog').hide();");
        }
        catch(EJBException ex) {
            addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
            logger.debug("Errore imprevisto {} durante il salvataggio dell'allegato {} dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), allegato, ordinativo, ex.getCausedByException());        
        }  
    }
    
    public void eliminaAllegato() {
        try {
            os.elimina(allegato);
            
            aggiornaAllegati();                        
        }
        catch(EJBException ex) {
            addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
            logger.debug("Errore imprevisto {} durante il l'eliminazione dell'allegato {} dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), allegato, ordinativo, ex.getCausedByException());        
        }  
    }
    
    public void eliminaAllegati() {
        try {
            if(!isEmpty(allegatiSelezionati))            
                os.elimina(allegatiSelezionati);

            aggiornaAllegati();
        } catch (EJBException ex) {
            addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
            logger.debug("Errore imprevisto {} durante il l'eliminazione dell'allegato {} dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), allegato, ordinativo, ex.getCausedByException());
        }  
    }
    
    public void salvaOrdinativo() {
        if(!ordinativo.changed()) {
            aggiornaOrdinativi();
            PrimeFaces.current().executeScript("PF('ordinativoDialog').hide();");
            return;
        }
        
        if(ordinativo.getImportoIva()!=null && ordinativo.getImportoRitenuta()!=null) {
            addMessage(Message.warn("Non è possibile per l'ordinativo indicare contemporaneamente l'importo IVA e l'importo ritenuta."));
            return;
        }
        
        List<Documento> lDoc = new ArrayList<>();
        if(documentFiles!=null) {
            for(UploadedFile f : documentFiles.getFiles()) {
                lDoc.add(new Documento(f.getFileName(), gruppo, f.getContent(), codServ.getMimeType(f.getContentType())));
            }
        }
        
        ordinativo.setIdCompetenza(ordinativoCapComp.getId());
        ordinativo.setIdCodice(ordinativoCodice.getId());
        if(ordinativoTipoRts!=null) ordinativo.setIdTipoRts(ordinativoTipoRts.getId());
        if(ordinativoTipoDocumento!=null) ordinativo.setIdTipoDocumento(ordinativoTipoDocumento.getId());
        
        try {
            if(ordinativo.getId()==null) {
                os.inserisci(ordinativo, lDoc);            
            }
            else {
                os.modifica(ordinativo);
            }
            
            aggiornaOrdinativi();
            
            PrimeFaces.current().executeScript("PF('ordinativoDialog').hide();");
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato aggiornato da un altro utente. Aggiornare e riprovare."));
            }
            else if(ex.getCausedByException() instanceof DuplicationException) {
                addMessage(Message.warn("Esiste già un ordinativo con i dati indicati."));                
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il salvataggio dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }  
    }
    
    public void elimina() {        
        try {
            os.elimina(ordinativo);
            aggiornaOrdinativi();
            addMessage(Message.info("Ordinativo eliminato correttamente."));
            PrimeFaces.current().executeScript("PF('deleteOrdinativoDialog').hide();");
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato già eliminato da un altro utente. Aggiornare e riprovare."));
            }            
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        } 
    }
    
    public boolean filtroGlobale(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (LangUtils.isBlank(filterText)) {
            return true;
        }
                
        OrdinativoRecord rec = (OrdinativoRecord)value;
        boolean match = contains(rec.getBeneficiario(), filterText)
            || contains(rec.getDescrizioneRts(), filterText)
            || contains(rec.getFatturaNumero(), filterText);
        if(isDate(filterText)) {
            LocalDate d = toDate(filterText);
            match = match || d.equals(rec.getDataDocumento())
                          || d.equals(rec.getDataPagamento())
                          || d.equals(rec.getFatturaData());
        }        
        match = match || contains(rec.getNumeroDocumento(), filterText);
        match = match || contains(rec.getNumeroPagamento(), filterText);
        match = match || contains(toStringFormat(rec.getImporto()), filterText);
        
        return match;
    } 
    
    public boolean isChiuso() {
        return (capCompSelezionato!=null) ? (capCompSelezionato.getChiuso()==1 || capCompSelezionato.getChiuso()==2) : false;
    }
    
    public void salvaOrdinativoIva() {
        ordinativo.setOrdinativoIva((ordinativoIva!=null) ? ordinativoIva.getId() : null);
        
        try {           
            os.modifica(ordinativo);
                       
            aggiornaOrdinativi();
            
            PrimeFaces.current().executeScript("PF('ordinativoIvaDialog').hide();");
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato aggiornato da un altro utente. Aggiornare e riprovare."));
            }
            else if(ex.getCausedByException() instanceof DuplicationException) {
                addMessage(Message.warn("Esiste già un ordinativo con i dati indicati."));                
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il salvataggio dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }
    }
    
    public void rimuoviOrdinativoIva() {
        this.ordinativoIva = null;
        this.capCompIvaSelezionato = null;
        this.ordinativiIva = null;
    }
    
    public void pulisciSelezione() {
        if(ordinativo!=null) {
            PrimeFaces.current().executeScript("PF('ordinativiTable').unselectAllRows()");
            ordinativo = null;
        }        
    }
    
    public void duplica() {
        OrdinativoRecord or = ordinativo.copy();
        or.setId(null);
        or.setVersione(1L);
        ordinativo = or;
        azione = "Duplica";
    }
    
    public void consolida() {
        ordinativo.setConsolidamento((byte)1);
        os.modifica(ordinativo);
        
        aggiornaOrdinativi();        
    }
    
    public boolean consolidabile(OrdinativoRecord o) {
        CodiceRecord c = decodeCodice(o.getIdCodice());
        return "pub".equalsIgnoreCase(c.getCodice()) && o.getConsolidamento()==0;
    }
    
    public String collegamentoMessage() {
        int cnt = (ordinativo!=null) ? os.contaOrdinativiIva(ordinativo) : 0;
        return (cnt>0) ? "\nL'ordinativo paga l'IVA di altri "+cnt+" ordinativo/i. Eliminandolo verrà rimosso ogni riferimento." : "";
    }
    
    public void onAllegatoRowEdit(RowEditEvent<OrdinativoRecord> event) {        
        os.modifica(allegato);            
        addMessage(Message.info("Descrizione allegato aggiornata."));            
    }
}
