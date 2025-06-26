/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.MovimentiVirtualiRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.RisultatoRicerca;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.MovimentiVirtualiService;
import it.usr.web.usrbilancio.service.OrdinativoService;
import it.usr.web.usrbilancio.service.QuietanzaService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.PrimeFaces;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class RicercaController extends BaseController {
    @Inject
    OrdinativoService os;
    @Inject
    QuietanzaService qs;
    @Inject
    MovimentiVirtualiService mvs;
    @Inject
    CodiceService codServ;
    @Inject
    CompetenzaService cs;
    List<RisultatoRicerca> risultato;    
    List<TipoRtsRecord> tipiRts;    
    TipoRtsRecord[] tipiRtsSelezionati;
    CodiceRecord[] codiciSelezionati;
    CapitoloCompetenza[] competenzeSelezionate;
    String[] tipologieSelezionate = { "O", "Q", "MV" };
    Map<Integer, CapitoloCompetenza> mCapComp;
    Map<Integer, TipoDocumentoRecord> mTipoDoc;
    Map<Integer, CodiceRecord> mCodici;
    Map<Integer, TipoRtsRecord> mTipiRts;
    List<CapitoloCompetenza> competenze;
    List<CodiceRecord> codici;
    String testo;
    LocalDate dataDa;
    LocalDate dataA;
    BigDecimal importoDa;
    BigDecimal importoA;
    Integer annoCompetenza;
    boolean dataAnd;
    boolean importoAnd;
    boolean completato;
    boolean tipiRtsAnd;
    boolean codiciAnd;
    boolean annoCompAnd;
    boolean competenzeAnd;
    BigDecimal totale;
    BigDecimal totaleOrdinativi;
    BigDecimal totaleQuietanze;
    BigDecimal totaleMovimenti;
    
    public void init() {
        risultato = null;
        testo = null;
        dataDa = null;
        dataA = null;
        importoDa = null;
        importoA = null;         
        tipiRtsSelezionati = null;
        codiciSelezionati = null;
        competenzeSelezionate = null;
        annoCompetenza = null;
        totale = null;
        totaleMovimenti = null;
        totaleQuietanze = null;
        totaleOrdinativi = null;
        importoAnd = true;        
        tipiRtsAnd = true;
        codiciAnd = true;
        dataAnd = true;
        annoCompAnd = true;
        competenzeAnd = true;
        
        mCapComp = new HashMap<>();
        mTipiRts = new HashMap<>();
        mCodici = new HashMap<>();
        
        tipiRts = codServ.getTipiRts(CodiceService.GruppoRts.RTS_TUTTI);        
        tipiRts.forEach(t -> mTipiRts.put(t.getId(), t));
        cs.getCapitoliCompetenze().forEach(cc -> mCapComp.put(cc.getId(), cc));        
        mTipoDoc = codServ.getTipiDocumentoAsMap();
        codici = codServ.getCodici();
        codici.forEach(c -> mCodici.put(c.getId(), c));
        competenze = cs.getCapitoliCompetenze();
        
        completato = false;
    }

    public List<TipoRtsRecord> getTipiRts() {
        return tipiRts;
    }

    public TipoRtsRecord[] getTipiRtsSelezionati() {
        return tipiRtsSelezionati;
    }

    public void setTipiRtsSelezionati(TipoRtsRecord[] tipiRtsSelezionati) {
        this.tipiRtsSelezionati = tipiRtsSelezionati;
    }

    public boolean isTipiRtsAnd() {
        return tipiRtsAnd;
    }

    public void setTipiRtsAnd(boolean tipiRtsAnd) {
        this.tipiRtsAnd = tipiRtsAnd;
    }

    public List<RisultatoRicerca> getRisultato() {
        return risultato;
    }
            
    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public LocalDate getDataDa() {
        return dataDa;
    }

    public void setDataDa(LocalDate dataDa) {
        this.dataDa = dataDa;
    }

    public LocalDate getDataA() {
        return dataA;
    }

    public void setDataA(LocalDate dataA) {
        this.dataA = dataA;
    }

    public BigDecimal getImportoDa() {
        return importoDa;
    }

    public void setImportoDa(BigDecimal importoDa) {
        this.importoDa = importoDa;
    }

    public BigDecimal getImportoA() {
        return importoA;
    } 

    public void setImportoA(BigDecimal importoA) {
        this.importoA = importoA;
    }

    public boolean isDataAnd() {
        return dataAnd;
    }

    public void setDataAnd(boolean dataAnd) {
        this.dataAnd = dataAnd;
    }

    public boolean isImportoAnd() {
        return importoAnd;
    }

    public void setImportoAnd(boolean importoAnd) {
        this.importoAnd = importoAnd;
    }

    public boolean isCodiciAnd() {
        return codiciAnd;
    }

    public void setCodiciAnd(boolean codiciAnd) {
        this.codiciAnd = codiciAnd;
    }
        
    public boolean isCompletato() {
        return completato;
    }
               
    public CapitoloCompetenza decodeCapComp(int id) {
        return mCapComp.get(id);
    }
    
    public CodiceRecord decodeCodice(int id) {
        return mCodici.get(id);
    }
    
    public TipoDocumentoRecord decodeTipoDocumento(int id) {
        return mTipoDoc.get(id);
    }

    public TipoRtsRecord decodeTipoRts(int id) {
        return mTipiRts.get(id);
    }

    public List<CodiceRecord> getCodici() {
        return codici;
    }

    public CodiceRecord[] getCodiciSelezionati() {
        return codiciSelezionati;
    }

    public void setCodiciSelezionati(CodiceRecord[] codiciSelezionati) {
        this.codiciSelezionati = codiciSelezionati;
    }

    public String[] getTipologieSelezionate() {
        return tipologieSelezionate;
    }

    public void setTipologieSelezionate(String[] tipologieSelezionate) {
        this.tipologieSelezionate = tipologieSelezionate;
    }

    public Integer getAnnoCompetenza() {
        return annoCompetenza;
    }

    public void setAnnoCompetenza(Integer annoCompetenza) {
        this.annoCompetenza = annoCompetenza;
    }

    public boolean isAnnoCompAnd() {
        return annoCompAnd;
    }

    public void setAnnoCompAnd(boolean annoCompAnd) {
        this.annoCompAnd = annoCompAnd;
    }

    public CapitoloCompetenza[] getCompetenzeSelezionate() {
        return competenzeSelezionate;
    }

    public void setCompetenzeSelezionate(CapitoloCompetenza[] competenzeSelezionate) {
        this.competenzeSelezionate = competenzeSelezionate;
    }

    public List<CapitoloCompetenza> getCompetenze() {
        return competenze;
    }

    public boolean isCompetenzeAnd() {
        return competenzeAnd;
    }

    public void setCompetenzeAnd(boolean competenzeAnd) {
        this.competenzeAnd = competenzeAnd;
    }

    public BigDecimal getTotale() {
        return totale!=null ? totale : BigDecimal.ZERO;
    }
                                      
    public BigDecimal getTotaleOrdinativi() {
        return totaleOrdinativi;
    }

    public BigDecimal getTotaleQuietanze() {
        return totaleQuietanze;
    }

    public BigDecimal getTotaleMovimenti() {
        return totaleMovimenti;
    }
            
    public void cerca() {
        completato = false;
        List<String> tipSel = Arrays.asList(tipologieSelezionate!=null ? tipologieSelezionate : new String[]{});
        
        if(dataDa!=null && dataA!=null && dataDa.isAfter(dataA)) {
            LocalDate d = dataDa;
            dataDa = dataA;
            dataA = d;
        }
        
        if(importoDa!=null && importoA!=null && importoDa.compareTo(importoA)>0) {
            BigDecimal i = importoDa;
            importoDa = importoA;
            importoA = i;
        }
        
        List<OrdinativoRecord> ordinativi = tipSel.contains("O") ? os.cerca(testo, dataAnd, dataDa, dataA, importoAnd, importoDa, importoA, tipiRtsAnd, tipiRtsSelezionati, codiciAnd, codiciSelezionati, annoCompAnd, annoCompetenza, competenzeAnd, competenzeSelezionate) : new ArrayList();
        List<QuietanzaRecord> quietanze = tipSel.contains("Q") ? qs.cerca(testo, dataAnd, dataDa, dataA, importoAnd, importoDa, importoA, tipiRtsAnd, tipiRtsSelezionati, codiciAnd, codiciSelezionati, annoCompAnd, annoCompetenza, competenzeAnd, competenzeSelezionate) : new ArrayList();
        List<MovimentiVirtualiRecord> movimenti = tipSel.contains("MV") ? mvs.cerca(testo, dataAnd, dataDa, dataA, importoAnd, importoDa, importoA, codiciAnd, codiciSelezionati, annoCompAnd, annoCompetenza, competenzeAnd, competenzeSelezionate) : new ArrayList();
         
        totale = BigDecimal.ZERO;
        totaleQuietanze = BigDecimal.ZERO;
        totaleOrdinativi = BigDecimal.ZERO;
        totaleMovimenti = BigDecimal.ZERO;
        risultato = new ArrayList<>();
        try {
            ordinativi.forEach(e -> {
                RisultatoRicerca r = new RisultatoRicerca(RisultatoRicerca.TipoRisultato.ORDINATIVO);
                r.setId(e.getId());
                r.setIdCodice(e.getIdCodice());
                r.setIdCompetenza(e.getIdCompetenza());
                r.setIdTipoDocumento(e.getIdTipoDocumento());
                r.setIdTipoRts(e.getIdTipoRts());
                r.setDataDocumento(e.getDataDocumento());
                r.setDataPagamento(e.getDataPagamento());
                r.setDescrizione(e.getDescrizioneRts());
                r.setImportoU(e.getImporto());            
                r.setNote(e.getNote());
                r.setNumeroDocumento(e.getNumeroDocumento());
                r.setNumeroPagamento(e.getNumeroPagamento());
                r.setSoggetto(e.getBeneficiario());

                totale = totale.subtract(e.getImporto());
                totaleOrdinativi = totaleOrdinativi.add(e.getImporto());

                risultato.add(r);
            });
        }
        catch(NullPointerException e) {}
        
        try {
            quietanze.forEach(e -> {
                RisultatoRicerca r = new RisultatoRicerca(RisultatoRicerca.TipoRisultato.QUIETANZA);
                r.setId(e.getId());
                r.setIdCodice(e.getIdCodice());
                r.setIdCompetenza(e.getIdCompetenza());
                r.setIdTipoDocumento(e.getIdTipoDocumento());
                r.setIdTipoRts(e.getIdTipoRts());
                r.setDataDocumento(e.getDataDocumento());
                r.setDataPagamento(e.getDataPagamento());
                r.setDescrizione(e.getDescrizioneOrdinanza());
                r.setImportoE(e.getImporto());            
                r.setNote(e.getNote());
                r.setNumeroDocumento(e.getNumeroDocumento());
                r.setNumeroPagamento(e.getNumeroPagamento());
                r.setSoggetto(e.getOrdinante());

                totale = totale.add(e.getImporto());
                totaleQuietanze = totaleQuietanze.add(e.getImporto());
                
                risultato.add(r);
            });
        }
        catch(NullPointerException e) {}
        
        try {
            movimenti.forEach(e -> {
                RisultatoRicerca r = new RisultatoRicerca(RisultatoRicerca.TipoRisultato.MOVIMENTO_VIRTUALE);
                r.setId(e.getId());
                r.setIdCodice(e.getIdCodice());
                r.setIdCompetenza(e.getIdCompetenza());
                r.setIdTipoDocumento(e.getIdTipoDocumento());
                r.setDataDocumento(e.getDataDocumento()); 
                r.setDataPagamento(e.getDataPagamento());
                r.setDescrizione(e.getDescrizioneRagioneria());
                r.setImportoV(e.getImporto());            
                r.setNote(e.getNote());
                r.setNumeroDocumento(e.getNumeroDocumento());
                r.setNumeroPagamento(e.getNumeroPagamento());    
                r.setSoggetto(e.getNominativo());
                
                totale = totale.add(e.getImporto());
                totaleMovimenti = totaleMovimenti.add(e.getImporto());
                
                risultato.add(r);
            });
        }
        catch(NullPointerException e) {}
        
        completato = true;
        PrimeFaces.current().executeScript("PF('criteriPanel').toggle();");
    }
    
    public long numeroOrdinativi() {
        return risultato!=null ? risultato.stream().filter(r -> r.getTipologia()==RisultatoRicerca.TipoRisultato.ORDINATIVO).count() : 0;
    }
    
    public long numeroQuietanze() {
        return risultato!=null ? risultato.stream().filter(r -> r.getTipologia()==RisultatoRicerca.TipoRisultato.QUIETANZA).count() : 0;
    }
    
    public long numeroMovimenti() {
        return risultato!=null ? risultato.stream().filter(r -> r.getTipologia()==RisultatoRicerca.TipoRisultato.MOVIMENTO_VIRTUALE).count() : 0;
    }
    
    public String decodeColor(RisultatoRicerca.TipoRisultato risultato) {
        return risultato!=null ? risultato.toString().toLowerCase() : "";
    }
    
    public String codiceTooltip(int idCodice) {
        CodiceRecord cr = decodeCodice(idCodice);
        String concluso = cr.getConcluso()!=null ? (cr.getConcluso() ? "SI" : "NO") : "--";
        StringBuilder sb = new StringBuilder();        
        sb.append(cr.getDescrizione()).append("<br/>");
        sb.append("Finanziamento: ").append(formatBigDecimal(cr.getImportoFinComm())).append("<br/>");
        sb.append("Definitivo: ").append(formatBigDecimal(cr.getImportoDefinitivo())).append("<br/>");
        sb.append("Cofinanziamento: ").append(formatBigDecimal(cr.getImportoCofinanziamento())).append("<br/>");
        sb.append("Conto-Termico: ").append(formatBigDecimal(cr.getImportoContotermico())).append("<br/>");
        sb.append("Concluso: ").append(concluso);
        return sb.toString();                
    }
    
    private String formatBigDecimal(BigDecimal bd) {
        return bd==null ? "--" : new DecimalFormat("#,##0.00").format(bd);
    }
     
    public String formattaConto(CodiceRecord c) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(c.getDescrizione());
        if(!isEmpty(c.getOrdinanza())) {
            sb.append(" [").append(c.getOrdinanza()).append("]");
        } 
         
        return sb.toString();
    }
} 
