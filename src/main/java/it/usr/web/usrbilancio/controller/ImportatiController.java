/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.DocumentService;
import it.usr.web.usrbilancio.service.OrdinativoAppoggioService;
import it.usr.web.usrbilancio.service.OrdinativoService;
import it.usr.web.usrbilancio.service.StaleRecordException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.ejb.EJBException;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.format.DateTimeFormatter;
import org.primefaces.PrimeFaces;
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
public class ImportatiController extends BaseController {
    @Inject
    OrdinativoService os;
    @Inject
    OrdinativoAppoggioService oas;
    @Inject
    CompetenzaService cs;
    @Inject
    CodiceService codServ;
    @Inject
    DocumentService ds;
    @Inject
    @AppLogger
    Logger logger;    
    List<OrdinativoAppoggioRecord> ordinativi;
    List<OrdinativoAppoggioRecord> ordinativiFiltrati;
    List<AllegatoAppoggioRecord> allegati;
    OrdinativoAppoggioRecord ordinativo;
    AllegatoAppoggioRecord allegato;
    boolean mostraTutti;
    boolean canTransferMulti;
    boolean multiDaConsolidare;
    CapitoloCompetenza ordinativoCapComp;
    CodiceRecord ordinativoCodice;
    TipoRtsRecord ordinativoTipoRts;
    TipoDocumentoRecord ordinativoTipoDocumento;
    List<CapitoloCompetenza> capComp;
    List<CapitoloCompetenza> capCompAperti;
    CapitoloCompetenza capCompSelezionato;
    Map<Integer, CapitoloCompetenza> mCampComp;
    Map<Integer, CodiceRecord> codici;
    Map<Integer, TipoRtsRecord> tipiRts;
    List<TipoRtsRecord> tipiRtsList;
    List<OrdinativoAppoggioRecord> ordinativiSelezionati;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    UploadedFiles documentFiles;
    BigDecimal parziale;
    BigDecimal totale;
    BigDecimal totaleIVA;
    BigDecimal parzialeIVA;
    String dataCaricamento;
    String gruppo;
    
    public void init() {
        mostraTutti = false;
        codici = codServ.getCodiciAsMap();
        tipiRtsList = codServ.getTipiRts(CodiceService.GruppoRts.RTS_ORDINATIVO);
        tipiRts = new HashMap<>();
        tipiRtsList.forEach(t -> tipiRts.put(t.getId(), t));
        List<TipoDocumentoRecord> lTipoDoc = codServ.getTipiDocumentoNuovi();
        tipiDocumento = new HashMap<>();
        lTipoDoc.forEach(d -> tipiDocumento.put(d.getId(), d));
        capComp = cs.getCapitoliCompetenzeApertiNonFuturi();  
        mCampComp = new HashMap<>();
        capComp.forEach(cc -> mCampComp.put(cc.getId(), cc));
        capCompAperti = cs.getCapitoliCompetenzeApertiNonFuturi();
                
        aggiornaOrdinativi();
    }

    public boolean isOrdinativoFlag() {
        return ordinativo!=null ? ((ordinativo.getFlag() & 1) == 1) : false;
    } 
 
    public void setOrdinativoFlag(boolean ordinativoFlag) {
        int currentFlag = ordinativo.getFlag();
        currentFlag = ordinativoFlag ? (currentFlag | 1) : (currentFlag & ~1);
        ordinativo.setFlag(currentFlag);
    }
    
    public String getDataCaricamento() {
        return dataCaricamento;
    }

    public void setDataCaricamento(String dataCaricamento) {
        this.dataCaricamento = dataCaricamento;
    }
        
    public CodiceService getCodServ() {
        return codServ;
    }

    public void setCodServ(CodiceService codServ) {
        this.codServ = codServ;
    }

    public List<OrdinativoAppoggioRecord> getOrdinativi() {
        return ordinativi;
    }

    public void setOrdinativi(List<OrdinativoAppoggioRecord> ordinativi) {
        this.ordinativi = ordinativi;
    }

    public OrdinativoAppoggioRecord getOrdinativo() {
        return ordinativo;
    }

    public List<CapitoloCompetenza> getCapCompAperti() {
        return capCompAperti;
    }
        
    public void setOrdinativo(OrdinativoAppoggioRecord ordinativo) {
        this.ordinativo = ordinativo;
        
        if(ordinativo!=null) {
            this.ordinativoCapComp = mCampComp.get(ordinativo.getIdCompetenza());
            this.ordinativoCodice = codici.get(ordinativo.getIdCodice());
            this.ordinativoTipoDocumento = tipiDocumento.get(ordinativo.getIdTipoDocumento());
            this.ordinativoTipoRts = tipiRts.get(ordinativo.getIdTipoRts());
        }
    }

    public List<OrdinativoAppoggioRecord> getOrdinativiSelezionati() {
        return ordinativiSelezionati;
    }

    public void setOrdinativiSelezionati(List<OrdinativoAppoggioRecord> ordinativiSelezionati) {
        this.ordinativiSelezionati = ordinativiSelezionati;
    }
        
    public CapitoloCompetenza getOrdinativoCapComp() {
        return ordinativoCapComp;
    }

    public void setOrdinativoCapComp(CapitoloCompetenza ordinativoCapComp) {
        this.ordinativoCapComp = ordinativoCapComp;
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

    public Map<Integer, CodiceRecord> getCodici() {
        return codici;
    }

    public void setCodici(Map<Integer, CodiceRecord> codici) {
        this.codici = codici;
    }
    
    public List<TipoRtsRecord> getTipiRtsList() {
        return tipiRtsList;
    }            

    public Map<Integer, TipoRtsRecord> getTipiRts() {
        return tipiRts;
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
 
    public CodiceRecord decodeCodice(int idCodice) {
        return codici.get(idCodice);
    }
    
    public TipoRtsRecord decodeTipoRts(int idTipoRts) {
        return tipiRts.get(idTipoRts);
    }
    
    public TipoDocumentoRecord decodeTipoDocumento(int idTipoDocumento) {
        return tipiDocumento.get(idTipoDocumento);
    }

    public CapitoloCompetenza decodeCapitoloCompetenza(int idCompetenza) {
        return mCampComp.get(idCompetenza);
    }
           
    public List<AllegatoAppoggioRecord> getAllegati() {
        return allegati;
    }

    public void setAllegati(List<AllegatoAppoggioRecord> allegati) {
        this.allegati = allegati;
    }

    public AllegatoAppoggioRecord getAllegato() {
        return allegato;
    }

    public void setAllegato(AllegatoAppoggioRecord allegato) {
        this.allegato = allegato;
    }
    
    public UploadedFiles getDocumentFiles() {
        return documentFiles;
    }

    public void setDocumentFiles(UploadedFiles documentFiles) {
        this.documentFiles = documentFiles;
    }

    public List<OrdinativoAppoggioRecord> getOrdinativiFiltrati() {
        return ordinativiFiltrati;
    }

    public void setOrdinativiFiltrati(List<OrdinativoAppoggioRecord> ordinativiFiltrati) {
        this.ordinativiFiltrati = ordinativiFiltrati;
    }

    public BigDecimal getParziale() {
        return parziale;
    }

    public void setParziale(BigDecimal parziale) {
        this.parziale = parziale;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }

    public BigDecimal getTotaleIVA() {
        return totaleIVA;
    }

    public BigDecimal getParzialeIVA() {
        return parzialeIVA;
    }

    public boolean isCanTransferMulti() {
        return canTransferMulti;
    }

    public String getGruppo() {
        return gruppo;
    }

    public void setGruppo(String gruppo) {
        this.gruppo = gruppo;
    }
            
    public String getDescrizioneOrdinativo() {
        if(ordinativo==null) return "";
        StringBuilder sb = new StringBuilder();
        return sb.append(ordinativo.getNumeroPagamento()).append(" DEL ").append(ordinativo.getDataPagamento().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                 .append(" (").append(ordinativo.getBeneficiario()).append(")").toString();        
    }
    
    public void aggiornaParziale() {
        parziale = BigDecimal.ZERO;
        parzialeIVA = BigDecimal.ZERO;
        if(!isEmpty(ordinativiFiltrati)) {
            ordinativiFiltrati.forEach(o -> {
                if(o.getImporto()!=null) parziale = parziale.add(o.getImporto());
                if(o.getImportoIva()!=null) parzialeIVA = parzialeIVA.add(o.getImportoIva());
            });
        }
    }
    
    public boolean isMostraTutti() {
        return mostraTutti;
    }

    public void setMostraTutti(boolean mostraTutti) {
        this.mostraTutti = mostraTutti;
    }

    public boolean isMultiDaConsolidare() {
        return multiDaConsolidare;
    }
            
    public boolean isTrasferibile(OrdinativoAppoggioRecord oa) {
        return oa.getIdCompetenza()!=null && 
                oa.getIdTipoRts()!=null &&
                oa.getIdCodice()!=null &&
                oa.getNumeroPagamento()!=null && 
                oa.getDataPagamento()!=null &&
                oa.getBeneficiario()!=null && 
                oa.getImporto()!=null;
    }
    
    public void aggiornaOrdinativi() {                        
        if(isEmpty(dataCaricamento)) {
            ordinativi = (mostraTutti) ? oas.getOrdinativi() : oas.getOrdinativiUtente(getUtente().getUsername());
        }
        else {
            LocalDate d = LocalDate.parse(dataCaricamento, DateTimeFormatter.ofPattern("yyyyMMdd"));
            ordinativi = oas.getOrdinativiDataUtente(d, getUtente().getUsername());
        }
        totale = BigDecimal.ZERO;
        totaleIVA = BigDecimal.ZERO;
        ordinativi.forEach(o -> {
            if(o.getImporto()!=null) totale = totale.add(o.getImporto());
            if(o.getImportoIva()!=null) totaleIVA = totaleIVA.add(o.getImportoIva());
        });
        parziale = totale;
        parzialeIVA = totaleIVA;
        ordinativo = null;
        ordinativiFiltrati = null;
        allegati = null;
        allegato = null;
        ordinativiSelezionati = null; 
        
        ordinativoCapComp = null;
        ordinativoCodice = null;
        ordinativoTipoRts = null;
        ordinativoTipoDocumento = null;
        capCompSelezionato = null;
        documentFiles = null;        
        canTransferMulti = false;   
        multiDaConsolidare = false;
        gruppo = null;
    }
    
    public void cambioMostraTutti(AjaxBehaviorEvent e) {
        if(!isEmpty(dataCaricamento)) {
            dataCaricamento = null;
            mostraTutti = true;
        }
        
        aggiornaOrdinativi();
    }
    
    public void aggiornaAllegati() {
        allegati = oas.getAllegatiOrdinativoAppoggio(ordinativo.getId());
    }
    
    public String getUrl() {
        return (allegato!=null) ? "/DocumentServlet?scope=O&id="+allegato.getId() : null;
    }
    
    public void pulisciAllegato() {
        allegato = null;
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
                    logger.debug("Errore imprevisto {} durante l'estrazione di un file P7M per l'ordinativo di appoggio {}. Errore: {}", ex.getCause().getClass(), ordinativo, ex.getCause());        
                }
            }
        }
        
        try {
            oas.aggiungi(lAll, ordinativo.getId());
            
            aggiornaAllegati();
        }
        catch(EJBException ex) {
            addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
            logger.debug("Errore imprevisto {} durante il salvataggio degli allegati dell'ordinativo di appoggio {}. Errore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());        
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
        
        ordinativo.setIdCompetenza(ordinativoCapComp!=null ? ordinativoCapComp.getId() : null);
        ordinativo.setIdCodice(ordinativoCodice!=null ? ordinativoCodice.getId() : null);
        ordinativo.setIdTipoRts(ordinativoTipoRts!=null ? ordinativoTipoRts.getId() : null);
        ordinativo.setIdTipoDocumento(ordinativoTipoDocumento!=null ? ordinativoTipoDocumento.getId() : null);
        
        try {            
            oas.modifica(ordinativo);
                        
            aggiornaOrdinativi();
            
            PrimeFaces.current().executeScript("PF('ordinativoDialog').hide();");
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("L'ordinativo di appoggio è stato eliminato. Aggiornare."));                
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il salvataggio dell'ordinativo {}. Errore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }  
    }
    
    public void eliminaOrdinativo() {
        try {
            oas.elimina(ordinativo);
            
            aggiornaOrdinativi();
            addMessage(Message.info("Ordinativo eliminato correttamente!"));
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("L'ordinativo è sato già rimosso da un altro utente. Aggiornare."));
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione dell'ordinativo {}. Errore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }
    }
    
    public void modificaAllegato() {
        try {
            if(allegato.changed()) {
                oas.modifica(allegato);
                aggiornaAllegati();                
            }
            
            PrimeFaces.current().executeScript("PF('descrizioneDocumentoDialog').hide();");
        }
        catch(EJBException ex) {
            addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
            logger.debug("Errore imprevisto {} durante il salvataggio dell'allegato {} dell'ordinativo {}. Errore: {}", ex.getCausedByException().getClass(), allegato, ordinativo, ex.getCausedByException());        
        }  
    }
    
    public void eliminaAllegato() {
        try {
            oas.elimina(allegato);
            
            aggiornaAllegati();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("L'allegato è sato già rimosso da un altro utente. Aggiornare."));
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione dell'allegato {}. Errore: {}", ex.getCausedByException().getClass(), allegato, ex.getCausedByException());
            }
        }
    }
    
    public void trasferisci(boolean completato) {
        try {
            os.trasferisci(ordinativo, completato);
        
            aggiornaOrdinativi();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn(ex.getCausedByException().getMessage()));
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il trasferimento dell'ordinativo {}. Errore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }
    }
    
    public void trasferisciConsolidare() {
        try {
            os.trasferisciConsolidato(ordinativo);
        
            aggiornaOrdinativi();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn(ex.getCausedByException().getMessage()));
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il trasferimento da consolidare dell'ordinativo {}. Errore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }
    }
    
    public boolean filtroGlobale(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (LangUtils.isBlank(filterText)) {
            return true;
        }
        
        OrdinativoAppoggioRecord rec = (OrdinativoAppoggioRecord)value;
        boolean match = contains(rec.getBeneficiario(), filterText)
            || contains(rec.getDescrizioneRts(), filterText);
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
    
    public String rowColor(OrdinativoAppoggioRecord o) {
        if(o==null) return "";
        if(isTrasferibile(o)) return "lightgreen";
        if(o.getDataElaborazione().equals(LocalDate.now())) return "lightyellow";
        return "";
    }
    
    public void trasferisciSelezionati(boolean completato) {        
        try {
            ordinativiSelezionati.forEach(o -> os.trasferisci(o, completato));
        
            aggiornaOrdinativi();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn(ex.getCausedByException().getMessage()));
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il trasferimento dell'ordinativo {}. Errore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }
    }        
    
    public void trasferisciConsolidatiSelezionati() {        
        try {
            ordinativiSelezionati.forEach(o -> os.trasferisciConsolidato(o));
        
            aggiornaOrdinativi();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn(ex.getCausedByException().getMessage()));
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il trasferimento con consolidamento dell'ordinativo {}. Errore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }
    }
    
    public void verificaTrasferibilita() {
        ordinativo = null;
        canTransferMulti = false;
        multiDaConsolidare = false;               
        if(ordinativiSelezionati!=null && !ordinativiSelezionati.isEmpty()) {            
            canTransferMulti = true;
            multiDaConsolidare = true;
            for(OrdinativoAppoggioRecord oar : ordinativiSelezionati) {
                canTransferMulti &= isTrasferibile(oar);        
                CapitoloCompetenza cc = mCampComp.get(oar.getIdCompetenza());
                multiDaConsolidare &= (cc!=null ? cc.getDaconsolidare()==1 : false);
                if(!canTransferMulti) {
                    multiDaConsolidare = false;
                    return;
                }
            }   
            
        }
    }
    
    public void duplica() {
        if(ordinativo!=null) {
            OrdinativoAppoggioRecord oo = ordinativo.copy();
            oo.setId(null);
            oo.setDescrizioneRts("COPIA-"+ordinativo.getDescrizioneRts());
            try {
                oas.inserisci(oo, new ArrayList<>());
        
                aggiornaOrdinativi();
                
                addMessage(Message.info("L'ordinativo "+oo.getNumeroPagamento()+" è stato duplicato."));
            }
            catch(EJBException ex) {
                if(ex.getCausedByException() instanceof StaleRecordException) {
                    addMessage(Message.warn(ex.getCausedByException().getMessage()));
                }
                else {
                    addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                    logger.debug("Errore imprevisto {} durante la duplicazione dell'ordinativo {}. Errore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
                }
            }
        }
    }
}
