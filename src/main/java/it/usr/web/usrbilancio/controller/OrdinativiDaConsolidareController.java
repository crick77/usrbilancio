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
import java.math.BigDecimal;
import java.util.ArrayList;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
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
public class OrdinativiDaConsolidareController extends BaseController { 
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
    BigDecimal totaleImporto;
    BigDecimal totaleImportoCons;
    BigDecimal importoDifferenza;
    
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

    public UploadedFiles getDocumentFiles() {
        return documentFiles;
    }

    public void setDocumentFiles(UploadedFiles documentFiles) {
        this.documentFiles = documentFiles;
    }

    public BigDecimal getTotaleImporto() {
        return totaleImporto;
    }

    public BigDecimal getTotaleImportoCons() {
        return totaleImportoCons;
    }

    public BigDecimal getImportoDifferenza() {
        return importoDifferenza;
    }
                 
    public void aggiornaOrdinativi() {
        ordinativi = os.getOrdinativiDaConsolidare();    
        ordinativo = null;
        ordinativiFiltrati = null;
        allegati = null;
        allegato = null;
        documentFiles = null;
        totaleImporto = null;
        totaleImportoCons = null;
        importoDifferenza = null;
        
        aggiornaTotali();
        
        PrimeFaces.current().executeScript("PF('ordinativiTable').clearFilters();");
    }
     
    public void aggiornaTotali() {
        totaleImporto = BigDecimal.ZERO;
        totaleImportoCons = BigDecimal.ZERO;
        List<OrdinativoRecord> lO = (ordinativiFiltrati!=null) ? ordinativiFiltrati : ordinativi;
        lO.forEach(o -> {
            totaleImporto = totaleImporto.add(o.getImporto());
            if(o.getImportoCons()!=null) totaleImportoCons = totaleImportoCons.add(o.getImportoCons());
        });
        
        importoDifferenza = totaleImporto.subtract(totaleImportoCons);
    }
    
    public void aggiornaAllegati() {
        allegati = os.getAllegatiOrdinativo(ordinativo.getId());
    }
    
    public boolean filtroGlobale(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (LangUtils.isBlank(filterText)) {
            return true;
        }
                
        OrdinativoRecord rec = (OrdinativoRecord)value;
        boolean match = contains(rec.getBeneficiario(), filterText)
            || contains(rec.getDescrizioneRts(), filterText) || contains(rec.getNumeroPagamento(), filterText) ;
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
        List<Documento> lAll = new ArrayList<>();
        if(documentFiles!=null) {
            for(UploadedFile f : documentFiles.getFiles()) {
                Documento d = new Documento(f.getFileName(), null, f.getContent(), codServ.getMimeType(f.getContentType()));
                lAll.add(d);
            }
        }
        ordinativo.setConsolidamento((byte)2);
        os.modifica(ordinativo, lAll);
        PrimeFaces.current().executeScript("PF('caricaDialog').hide();");
        
        aggiornaOrdinativi(); 
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
        }
        catch(EJBException ex) {
            addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
            logger.debug("Errore imprevisto {} durante il salvataggio degli allegati dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());        
        }  
    }
    
    public boolean isChiuso() {
        if(ordinativo==null) return false;
        CapitoloCompetenza cc = mCampComp.get(ordinativo.getIdCompetenza());
        return (cc!=null) ? cc.getChiuso()==1 : true;
    }
    
    public CapitoloCompetenza decodeCapComp(int idCapComp) {
        return mCampComp.get(idCapComp);
    }
          
    public void onCellEdit(CellEditEvent event) {
        String oldValue = event.getOldValue()!=null ? String.valueOf(event.getOldValue()) : null;
        String newValue = event.getNewValue()!=null ? String.valueOf(event.getNewValue()) : null;

        if (!stringEquals(newValue, oldValue)) {
            if(ordinativo==null) {
                addMessage(Message.warn("Selezionare l'ordinativo prima di modificare la riga!"));            
                return;
            }
             
            os.modifica(ordinativo);  
            ordinativo = null;
            
            /*BigDecimal bdOld = new BigDecimal(oldValue);
            BigDecimal bdNew = new BigDecimal(newValue);
            BigDecimal diff = bdOld.subtract(bdNew);
            totaleImportoCons = totaleImportoCons.add(diff);
            importoDifferenza = importoDifferenza.add(diff); 
             
            PrimeFaces.current().ajax().update("form:ordinativi");*/
            
            addMessage(Message.info("Informazioni aggiornate."));             
        } 
    }
    
    public boolean filterByConto(Object value, Object filter, Locale locale) {
        String f = (String)filter;
        String d = (String)value; 
        if(d==null || f==null) return false;
        return (d.toUpperCase().contains(f.toUpperCase()));
    } 
}
  