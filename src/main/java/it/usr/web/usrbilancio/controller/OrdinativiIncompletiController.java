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
import it.usr.web.usrbilancio.service.OrdinativoService;
import jakarta.ejb.EJBException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
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
public class OrdinativiIncompletiController extends BaseController { 
    @Inject
    OrdinativoService os;
    @Inject
    CodiceService codServ;
    @Inject
    CompetenzaService cs;
    @Inject
    DocumentService ds;
    @Inject
    @AppLogger
    Logger logger;
    List<OrdinativoRecord> ordinativi;
    List<OrdinativoRecord> ordinativiFiltrati; 
    List<OrdinativoRecord> ordinativiSelezionati;
    List<AllegatoRecord> allegati;
    List<CapitoloCompetenza> capComp;
    Map<Integer, CapitoloCompetenza> mCampComp;
    Map<Integer, CodiceRecord> codici;
    Map<Integer, TipoRtsRecord> tipiRts;
    List<TipoRtsRecord> tipiRtsList;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    OrdinativoRecord ordinativo;
    AllegatoRecord allegato;
    UploadedFiles documentFiles;
    
    public void init() {
        codici = codServ.getCodiciAsMap();
        tipiRtsList = codServ.getTipiRts(CodiceService.GruppoRts.RTS_ORDINATIVO);
        tipiRts = new HashMap<>();
        tipiRtsList.forEach(t -> tipiRts.put(t.getId(), t));
        tipiDocumento = codServ.getTipiDocumentoAsMap();
        capComp = cs.getCapitoliCompetenze();  
        mCampComp = new HashMap<>();
        capComp.forEach(cc -> mCampComp.put(cc.getId(), cc));
        documentFiles = null;
        
        aggiornaOrdinativi();
    }
    
    public List<OrdinativoRecord> getOrdinativiFiltrati() {
        return ordinativiFiltrati;
    }

    public void setOrdinativiFiltrati(List<OrdinativoRecord> ordinativiFiltrati) {
        this.ordinativiFiltrati = ordinativiFiltrati;
    }

    public List<AllegatoRecord> getAllegati() {
        return allegati;
    }
        
    public OrdinativoRecord getOrdinativo() {
        return ordinativo;
    }

    public void setOrdinativo(OrdinativoRecord ordinativo) {
        this.ordinativo = ordinativo;
    }
                
    public List<OrdinativoRecord> getOrdinativi() {
        return ordinativi;
    }
 
    public List<CapitoloCompetenza> getCapComp() {
        return capComp;
    }

    public Map<Integer, CapitoloCompetenza> getmCampComp() {
        return mCampComp;
    }
    
    public Map<Integer, CodiceRecord> getCodici() {
        return codici;
    }

    public Map<Integer, TipoRtsRecord> getTipiRts() {
        return tipiRts;
    }

    public List<TipoRtsRecord> getTipiRtsList() {
        return tipiRtsList;
    }
        
    public Map<Integer, TipoDocumentoRecord> getTipiDocumento() {
        return tipiDocumento;
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

    public List<OrdinativoRecord> getOrdinativiSelezionati() {
        return ordinativiSelezionati;
    }

    public void setOrdinativiSelezionati(List<OrdinativoRecord> ordinativiSelezionati) {
        this.ordinativiSelezionati = ordinativiSelezionati;
    }
                    
    public void aggiornaOrdinativi() {
        ordinativi = os.getOrdinativiIncompleti();   
        ordinativo = null;
        ordinativiFiltrati = null;
        allegati = null;
        allegato = null;
        ordinativiSelezionati = null;
        
        PrimeFaces.current().executeScript("PF('ordinativiTable').clearFilters();");
    }
    
    public void aggiornaAllegati() {
        allegati = os.getAllegatiOrdinativo(ordinativo.getId());
        allegato = null;
        PrimeFaces.current().executeScript("PF('documentiDialog').show()");
    }
    
    public boolean filtroGlobale(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (LangUtils.isBlank(filterText)) {
            return true;
        }
                
        OrdinativoRecord rec = (OrdinativoRecord)value;
        boolean match = contains(rec.getBeneficiario(), filterText)
            || contains(rec.getDescrizioneRts(), filterText);
        if(isDate(filterText)) {
            LocalDate d = toDate(filterText);
            match = match || d.equals(rec.getDataDocumento())
                          || d.equals(rec.getDataPagamento())
                          || d.equals(rec.getDataRicevimento());
        }
        
        return match;
    }
    
    public void pulisciSelezione() {
        if(ordinativo!=null) {
            PrimeFaces.current().executeScript("PF('ordinativiTable').unselectAllRows()");
            ordinativo = null;
        }        
    }
    
    public void pulisciAllegato() {
        allegato = null;
    }
    
    public void pulisciOrdinativo() {
        ordinativo = null;
    }
    
    public void completa() {
        ordinativo.setRtsCompleto((byte)1);
        os.modifica(ordinativo);
        
        aggiornaOrdinativi();
    }
    public boolean isChiuso() {
        CapitoloCompetenza capCompSelezionato = mCampComp.get(ordinativo.getIdCompetenza());
        return (capCompSelezionato!=null) ? capCompSelezionato.getChiuso()==1 : true;
    }
    
    public void aggiungiAllegati() {
        List<Documento> lAll = new ArrayList<>();
        for(UploadedFile f : documentFiles.getFiles()) {
            Documento d = new Documento(f.getFileName(), null, f.getContent(), codServ.getMimeType(f.getContentType()));
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
    
    public void onAllegatoRowEdit(RowEditEvent<OrdinativoRecord> event) {        
        os.modifica(allegato);            
        addMessage(Message.info("Descrizione allegato aggiornata."));            
    }
    
    public void segnaCompletati() {
        ordinativiSelezionati.forEach(o -> o.setRtsCompleto((byte) 1));
        os.modifica(ordinativiSelezionati);
        
        aggiornaOrdinativi();
    }
}
