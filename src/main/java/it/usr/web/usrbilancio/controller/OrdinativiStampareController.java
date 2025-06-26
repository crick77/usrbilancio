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
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.OrdinativoService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.util.LangUtils;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class OrdinativiStampareController extends BaseController { 
    private final static String[] ROW_CODE = {"even", "odd"};
    @Inject
    OrdinativoService os;
    @Inject
    CodiceService codServ;
    @Inject
    CompetenzaService cs;
    @Inject
    @AppLogger
    Logger logger;
    List<OrdinativoRecord> ordinativi;
    List<OrdinativoRecord> ordinativiFiltrati; 
    List<AllegatoRecord> allegati;
    List<CapitoloCompetenza> capComp;
    Map<Integer, CapitoloCompetenza> mCampComp;
    Map<Integer, CodiceRecord> codici;
    Map<Integer, TipoRtsRecord> tipiRts;
    List<TipoRtsRecord> tipiRtsList;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    OrdinativoRecord ordinativo;
    AllegatoRecord allegato;
    OrdinativoRecord ordinativoPrecedente; 
    int rowNum;
    
    public void init() {
        codici = codServ.getCodiciAsMap();
        tipiRtsList = codServ.getTipiRts(CodiceService.GruppoRts.RTS_ORDINATIVO);
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
            
    public void aggiornaOrdinativi() {
        ordinativi = os.getOrdinativiDaStampare();   
        ordinativo = null;
        ordinativiFiltrati = null;
        allegati = null;
        allegato = null;
        ordinativoPrecedente = null;
        
        PrimeFaces.current().executeScript("PF('ordinativiTable').clearFilters();");
    }
    
    public void aggiornaAllegati() {
        allegati = os.getAllegatiOrdinativo(ordinativo.getId());
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
    
    public void stampa() {
        ordinativo.setRtsStampato((byte)1);
        os.modifica(ordinativo);
        logger.info("L'ordinativo ID=[{o}] è stato flaggato come 'STAMPATO'.", ordinativo.getId());
        
        aggiornaOrdinativi();
    }
    
    public CapitoloCompetenza decodeCapComp(int idCapComp) {
        return mCampComp.get(idCapComp);
    } 
    
    public void onCellEdit(CellEditEvent event) {
        String oldValue = event.getOldValue()!=null ? String.valueOf(event.getOldValue()) : null;
        String newValue = event.getNewValue()!=null ? String.valueOf(event.getNewValue()) : null;

        if (!stringEquals(newValue, oldValue)) {
            os.modifica(ordinativo);            
            addMessage(Message.info("Note ordinativo aggiornate."));            
        }
    }
    
    public void onOrdinativoRowEdit(RowEditEvent<OrdinativoRecord> event) {        
        os.modifica(ordinativo);            
        addMessage(Message.info("Note ordinativo aggiornate."));            
    }
    
    public void onAllegatoRowEdit(RowEditEvent<OrdinativoRecord> event) {        
        os.modifica(allegato);            
        addMessage(Message.info("Descrizione allegato aggiornata."));            
    }
    
    public String rowColor(OrdinativoRecord o) {
        if(o==null || (isEmpty(o.getNumeroDocumento()) && o.getDataDocumento()==null)) return "single";
        
        if(ordinativoPrecedente==null) {
            ordinativoPrecedente = o;
        }
            
        if(!stringEquals(o.getNumeroDocumento(), ordinativoPrecedente.getNumeroDocumento())) {
            rowNum++;
            ordinativoPrecedente = o;
        }
        
        return ROW_CODE[rowNum%2];                
    } 
}
