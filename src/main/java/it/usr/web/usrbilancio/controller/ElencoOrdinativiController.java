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
import jakarta.ejb.EJBException;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
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
import java.util.Objects;
import java.util.StringJoiner;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.LangUtils;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class ElencoOrdinativiController extends BaseController {

    @Inject
    DocumentService ds;
    @Inject
    OrdinativoService os;
    @Inject
    CompetenzaService cs;
    @Inject
    CodiceService codServ;
    @Inject
    @AppLogger
    Logger logger;
    List<OrdinativoRecord> ordinativi;
    List<OrdinativoRecord> ordinativiFiltrati;
    List<OrdinativoRecord> ordinativiIva;
    List<AllegatoRecord> allegati;
    List<AllegatoRecord> allegatiSelezionati;
    AllegatoRecord allegato;
    List<CapitoloCompetenza> capComp;
    List<CapitoloCompetenza> capCompAperti;
    Map<Integer, CapitoloCompetenza> mCampComp;
    List<CodiceRecord> codici;
    Map<Integer, CodiceRecord> codiciMap;
    List<TipoRtsRecord> tipiRtsList;
    Map<Integer, TipoRtsRecord> tipiRts;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    CapitoloCompetenza ordinativoCapComp;
    CapitoloCompetenza ordinativoCapCompDest;
    CapitoloCompetenza capCompIvaSelezionato;
    OrdinativoRecord ordinativoIva;
    CodiceRecord ordinativoCodice;
    TipoRtsRecord ordinativoTipoRts;
    TipoDocumentoRecord ordinativoTipoDocumento;
    OrdinativoRecord ordinativo;
    Integer ordinativoCodiceFiltro;
    List<FilterMeta> filterBy;
    String[] selectedCapitoli;
    List<String> capitoli;
    BigDecimal totale;
    UploadedFiles documentFiles;
    OrdinativoRecord ordinativoImponibile;
    String azione;
    String gruppo;    

    public void init() {
        codici = codServ.getCodici();
        codiciMap = new HashMap<>();
        codici.forEach(c -> codiciMap.put(c.getId(), c));
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
        capCompAperti = cs.getCapitoliCompetenzeAperti();

        ordinativoCapComp = null;
        ordinativoCapCompDest = null;
        capCompIvaSelezionato = null;
        ordinativoCodice = null;
        ordinativoTipoDocumento = null;
        ordinativoTipoRts = null;
        allegati = null;
        allegato = null;
        allegatiSelezionati = null;                
        ordinativiIva = null;
        documentFiles = null;
        selectedCapitoli = null;
        ordinativoCodiceFiltro = null;

        filterBy = new ArrayList<>();

        aggiornaOrdinativi();
        clearFilters(false);
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
        
    public List<CapitoloCompetenza> getCapCompAperti() {
        return capCompAperti;
    }

    public List<OrdinativoRecord> getOrdinativi() {
        return ordinativi; 
    }

    public List<CapitoloCompetenza> getCapComp() {
        return capComp;
    }

    public List<OrdinativoRecord> getOrdinativiFiltrati() {
        return ordinativiFiltrati;
    }

    public void setOrdinativiFiltrati(List<OrdinativoRecord> ordinativiFiltrati) {
        this.ordinativiFiltrati = ordinativiFiltrati;
    }

    public OrdinativoRecord getOrdinativo() {
        return ordinativo;
    }

    public boolean isOrdinativoFlag() {
        return ordinativo!=null ? ((ordinativo.getFlag() & 1) == 1) : false;
    } 
 
    public void setOrdinativoFlag(boolean ordinativoFlag) {
        int currentFlag = ordinativo.getFlag();
        currentFlag = ordinativoFlag ? (currentFlag | 1) : (currentFlag & ~1);
        ordinativo.setFlag(currentFlag);
    }
    
    public boolean isCompleto() {
        return ordinativo!=null ? ordinativo.getRtsCompleto()==1 : false;
    }
    
    public void setCompleto(boolean completo) {
        if(ordinativo!=null) {
            ordinativo.setRtsCompleto((byte)(completo ? 1 : 0));
            if(!completo) ordinativo.setConsolidamento((byte)0);
        }
    }
    
    public boolean isConsolidato() {
        return ordinativo!=null ? ordinativo.getConsolidamento()==2 : false;
    }
    
    public void setConsolidato(boolean consolidato) {
        if(ordinativo!=null) {
            if(ordinativo.getRtsCompleto()==0) {
                ordinativo.setConsolidamento((byte)0);
            }
            else {
                ordinativo.setConsolidamento((byte)(consolidato ? 2 : 1));
            }
        }
    }

    public Integer getOrdinativoCodiceFiltro() {
        return ordinativoCodiceFiltro;
    }

    public void setOrdinativoCodiceFiltro(Integer ordinativoCodiceFiltro) {
        this.ordinativoCodiceFiltro = ordinativoCodiceFiltro;
    }
            
    public void setOrdinativo(OrdinativoRecord ordinativo) { 
        this.ordinativo = ordinativo;

        if (ordinativo != null) {
            ordinativoCapComp = mCampComp.get(ordinativo.getIdCompetenza());
            ordinativoCodice = codiciMap.get(ordinativo.getIdCodice());
            ordinativoTipoDocumento = tipiDocumento.get(ordinativo.getIdTipoDocumento());
            ordinativoTipoRts = tipiRts.get(ordinativo.getIdTipoRts());
            ordinativoCapCompDest = null;

            if (ordinativo.getOrdinativoIva() != null) {
                OrdinativoRecord o = os.getOrdinativoById(ordinativo.getOrdinativoIva());
                this.capCompIvaSelezionato = cs.getCapitoloCompetenzaById(o.getIdCompetenza()); 
                aggiornaIvaOrdinativi();
                this.ordinativoIva = o;
            } else {
                this.ordinativoIva = null;
                this.capCompIvaSelezionato = null;
            }
            azione = "Modifica";
        }
    }

    public String[] getSelectedCapitoli() {
        return selectedCapitoli; 
    }

    public void setSelectedCapitoli(String[] selectedCapitoli) {
        this.selectedCapitoli = selectedCapitoli;
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

    public CapitoloCompetenza getOrdinativoCapComp() {
        return ordinativoCapComp;
    }

    public void setOrdinativoCapComp(CapitoloCompetenza ordinativoCapComp) {
        this.ordinativoCapComp = ordinativoCapComp;
    }

    public CodiceRecord getOrdinativoCodice() {
        return this.ordinativoCodice;
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

    public List<AllegatoRecord> getAllegati() {
        return allegati;
    }

    public AllegatoRecord getAllegato() {
        return allegato;
    }

    public void setAllegato(AllegatoRecord allegato) {
        this.allegato = allegato;
    }

    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public List<OrdinativoRecord> getOrdinativiIva() {
        return ordinativiIva;
    }

    public void setOrdinativiIva(List<OrdinativoRecord> ordinativiIva) {
        this.ordinativiIva = ordinativiIva;
    }

    public CapitoloCompetenza getCapCompIvaSelezionato() {
        return capCompIvaSelezionato;
    }

    public void setCapCompIvaSelezionato(CapitoloCompetenza capCompIvaSelezionato) {
        this.capCompIvaSelezionato = capCompIvaSelezionato;
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

    public CapitoloCompetenza getOrdinativoCapCompDest() {
        return ordinativoCapCompDest;
    }

    public void setOrdinativoCapCompDest(CapitoloCompetenza ordinativoCapCompDest) {
        this.ordinativoCapCompDest = ordinativoCapCompDest;
    }

    public UploadedFiles getDocumentFiles() {
        return documentFiles;
    }

    public void setDocumentFiles(UploadedFiles documentFiles) {
        this.documentFiles = documentFiles;
    }

    public OrdinativoRecord getOrdinativoImponibile() {
        return ordinativoImponibile;
    }

    public void setOrdinativoImponibile(OrdinativoRecord ordinativoImponibile) {
        this.ordinativoImponibile = ordinativoImponibile;
    }

    public List<String> getCapitoli() {
        return capitoli;
    }

    public String getGruppo() {
        return gruppo;
    }
 
    public void setGruppo(String gruppo) {
        this.gruppo = gruppo;
    }
        
    public boolean isChiuso() {
        if (ordinativo != null) {
            CapitoloCompetenza cc = mCampComp.get(ordinativo.getIdCompetenza());
            return (cc != null) ? (cc.getChiuso() == 1 ||  cc.getChiuso() == 2) : false;
        } else {
            return false;
        }
    }

    public boolean isChiuso(OrdinativoRecord o) {
        if (o != null) {
            CapitoloCompetenza cc = mCampComp.get(o.getIdCompetenza());
            return (cc != null) ? (cc.getChiuso() == 1 ||  cc.getChiuso() == 2) : false;
        } else {
            return false;
        }
    }

    public void nuovo() {
        azione = "Nuovo";
        ordinativo = new OrdinativoRecord();
        ordinativo.setRtsCompleto((byte)0);
        ordinativo.setRtsStampato((byte)0);
        ordinativo.setConsolidamento((byte)0);
        ordinativo.setFlag(0);
        ordinativo.setVersione(1L);
        ordinativoCapComp = null;
        ordinativoCodice = null; 
        ordinativoTipoDocumento = null;
        ordinativoTipoRts = null;
        ordinativoCapCompDest = null;     
        gruppo = null;
    }
    
    public void aggiornaOrdinativi() {
        ordinativi = os.getOrdinativi();
        totale = BigDecimal.ZERO;
        if (!isEmpty(ordinativi)) {
            ordinativi.forEach(of -> totale = totale.add(of.getImporto()));
        }

        ordinativiFiltrati = null;
        ordinativo = null;
        ordinativoIva = null;
        ordinativoImponibile = null;
        azione = null;
        gruppo = null;
    }

    public void aggiornaAllegati() {
        allegati = os.getAllegatiOrdinativo(ordinativo.getId());
        allegato = null;
        allegatiSelezionati = null;
        PrimeFaces.current().executeScript("PF('documentiDialog').show()");
    }

    public void aggiornaTotale() {
        totale = BigDecimal.ZERO;
        if (!isEmpty(ordinativiFiltrati)) {
            ordinativiFiltrati.forEach(of -> totale = totale.add(of.getImporto()));
        }
    }

    public void pulisciAllegato() {
        allegato = null;
        allegatiSelezionati = null;
    }

    public boolean filtroGlobale(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (LangUtils.isBlank(filterText)) {
            return true;
        }

        OrdinativoRecord rec = (OrdinativoRecord) value;
        boolean match = contains(rec.getBeneficiario(), filterText)
                || contains(rec.getDescrizioneRts(), filterText);
        if (isDate(filterText)) {
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

    public void consolida() {
        ordinativo.setConsolidamento((byte) 1);
        if(ordinativo.getImportoCons()==null) ordinativo.setImportoCons(ordinativo.getImporto());
        os.modifica(ordinativo);

        aggiornaOrdinativi();
    }

    public boolean consolidabile(OrdinativoRecord o) {
        CodiceRecord c = decodeCodice(o.getIdCodice());
        return "pub".equalsIgnoreCase(c.getCodice()) && o.getConsolidamento() == 0;
    }

    public String collegamentoMessage() {
        int cnt = (ordinativo != null) ? os.contaOrdinativiIva(ordinativo) : 0;
        return (cnt > 0) ? "\nL'ordinativo paga l'IVA di altri " + cnt + " ordinativo/i. Eliminandolo verrà rimosso ogni riferimento." : "";
    }

    public void aggiornaIvaOrdinativi() {
        ordinativiIva = os.getOrdinativiByCompetenza(capCompIvaSelezionato.getId());
        for (int i = 0; i < ordinativiIva.size(); i++) {
            if (Objects.equals(ordinativiIva.get(i).getId(), ordinativo.getId())) {
                ordinativiIva.remove(i);
                break;
            }
        }
        ordinativoIva = null;
    }

    public void duplica() {
        OrdinativoRecord or = ordinativo.copy();
        if (isChiuso()) {
            or.setIdCompetenza(null);
        }
        or.setId(null);
        or.setVersione(1L);
        or.setDescrizioneRts("COPIA-"+ordinativo.getDescrizioneRts());
        ordinativo = or;
        azione = "Duplica";
    }

    public void salva() {
        if (!ordinativo.changed()) {
            aggiornaOrdinativi();
            PrimeFaces.current().executeScript("PF('ordinativoDialog').hide();");
            return;
        }

        if(ordinativo.getImportoIva()!=null && ordinativo.getImportoRitenuta()!=null) {
            addMessage(Message.warn("Non è possibile per l'ordinativo indicare contemporaneamente l'importo IVA e l'importo ritenuta."));
            return;
        }
        
        if (ordinativoCapCompDest != null) {
            ordinativo.setIdCompetenza(ordinativoCapCompDest.getId());
        }
        ordinativo.setIdCodice(ordinativoCodice.getId());
        if (ordinativoTipoRts != null) {
            ordinativo.setIdTipoRts(ordinativoTipoRts.getId());
        }
        if (ordinativoTipoDocumento != null) {
            ordinativo.setIdTipoDocumento(ordinativoTipoDocumento.getId());
        }

        List<Documento> lDoc = new ArrayList<>();
        if(documentFiles!=null) {
            for(UploadedFile f : documentFiles.getFiles()) {
                lDoc.add(new Documento(f.getFileName(), gruppo, f.getContent(), codServ.getMimeType(f.getContentType())));
            }
        }
        
        try {
            // se in consolidaento, copia l'importo nel valore da consolidare
            if(ordinativo.getConsolidamento()==1) {
                if(ordinativo.getImportoCons()==null) ordinativo.setImportoCons(ordinativo.getImporto());
            }
            
            if ("modifica".equalsIgnoreCase(azione)) {
                os.modifica(ordinativo);
            } else {
                os.inserisci(ordinativo, lDoc);
            }

            aggiornaOrdinativi();

            PrimeFaces.current().executeScript("PF('ordinativoDialog').hide();");
        } catch (EJBException ex) {
            if (ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato aggiornato da un altro utente. Aggiornare e riprovare."));
            } else if (ex.getCausedByException() instanceof DuplicationException) {
                addMessage(Message.warn("Esiste già un ordinativo con i dati indicati."));
            } else {
                addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il salvataggio dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }
    }

    public void aggiungiAllegati() {
        List<Documento> lAll = new ArrayList<>();
        for (UploadedFile f : documentFiles.getFiles()) {
            Documento d = new Documento(f.getFileName(), gruppo, f.getContent(), codServ.getMimeType(f.getContentType()));
            lAll.add(d);

            // Se P7M sbusta e aggiunge
            if (d.getFileName().toLowerCase().endsWith("p7m")) {
                try {
                    Documento extr = ds.extractP7M(d);
                    if (extr != null) {
                        lAll.add(extr);
                    } else {
                        logger.debug("L'estrazione del file P7M {} non è andata a buon fine.", d.getFileName());
                    }
                } catch (Exception ex) {
                    logger.debug("Errore imprevisto {} durante l'estrazione di un file P7M per l'ordinativo {}. Errrore: {}", ex.getCause().getClass(), ordinativo, ex.getCause());
                }
            }
        }

        try {
            os.inserisci(lAll, ordinativo.getId());

            aggiornaAllegati();
            
            PrimeFaces.current().executeScript("PF('allegatiDialog').hide()");
        } catch (EJBException ex) {
            addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
            logger.debug("Errore imprevisto {} durante il salvataggio degli allegati dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
        }
    }

    public void modificaAllegato() {
        try {
            if (allegato.changed()) {
                os.modifica(allegato);
                aggiornaAllegati();
            }

            PrimeFaces.current().executeScript("PF('descrizioneDocumentoDialog').hide();");
        } catch (EJBException ex) {
            addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
            logger.debug("Errore imprevisto {} durante il salvataggio dell'allegato {} dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), allegato, ordinativo, ex.getCausedByException());
        }
    }

    public void eliminaAllegato() {
        try {
            os.elimina(allegato);

            aggiornaAllegati();
        } catch (EJBException ex) {
            addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
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
    
    public void elimina() {
        try {
            os.elimina(ordinativo);
            aggiornaOrdinativi();
            addMessage(Message.info("Ordinativo eliminato correttamente."));
            PrimeFaces.current().executeScript("PF('deleteOrdinativoDialog').hide();");
        } catch (EJBException ex) {
            if (ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato già eliminato da un altro utente. Aggiornare e riprovare."));
            } else {
                addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione dell'ordinativo {}. Errrore: {}", ex.getCausedByException().getClass(), ordinativo, ex.getCausedByException());
            }
        }
    }

    public boolean isOrdinativoIVA(OrdinativoRecord or) {
        if (or == null) {
            return false;
        }
        return or.getOrdinativoIva() != null;
    }

    public boolean isImponibile(OrdinativoRecord or) {
        if (or == null) {
            return false;
        }
        return (or.getOrdinativoIva() == null && !isEmpty(or.getFatturaNumero()) && or.getFatturaData() != null);
    }

    public void recuperaOrdinativoImponibile(OrdinativoRecord o) {
        if (o != null) {
            ordinativoImponibile = os.getOrdinativoOrdinativoIva(o.getId());
            PrimeFaces.current().executeScript("PF('ordinativoImponibileDialog').show();");
        } else {
            ordinativoImponibile = null; 
        }
    }

    public boolean filterFunctionCapitoli(Object value, Object filter, Locale locale) {
        if (value == null || !(value instanceof String)) {
            return true;
        }
        String sVal = (String) value;
        if (selectedCapitoli == null || selectedCapitoli.length == 0) {
            return false;
        }
        for (String sel : selectedCapitoli) {
            if (sel.equalsIgnoreCase(sVal)) {
                return true;
            }
        }

        return false; 
    }    
    
    public void clearFilters(boolean resetFilters) {
        capitoli = new ArrayList<>();
        capComp.forEach(cc -> {
            capitoli.add(cc.getAnno() + " | " + cc.getDescrizione());
        });
        selectedCapitoli = new String[capitoli.size()];
        capitoli.toArray(selectedCapitoli);
        ordinativoCodiceFiltro = null;

        if (resetFilters) {
            PrimeFaces.current().executeScript("PF('ordinativiTable').clearFilters();PF('capcompcbm').checkAll()");
        }
    }

    public String exportCompetenza(UIColumn column) {
        String value = "";
        for (UIComponent child : column.getChildren()) {
            if (child instanceof ValueHolder) {
                value = ComponentUtils.getValueToRender(FacesContext.getCurrentInstance(), child);
            }
        }

        return value + "_TEST";
    }
    
    public void onAllegatoRowEdit(RowEditEvent<OrdinativoRecord> event) {        
        os.modifica(allegato);            
        addMessage(Message.info("Descrizione allegato aggiornata."));            
    }
}
