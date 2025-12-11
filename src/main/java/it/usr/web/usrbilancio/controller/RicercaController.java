/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoRecord;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.MovimentiVirtualiRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.DocumentoAllegato;
import it.usr.web.usrbilancio.model.RisultatoRicerca;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.MovimentiVirtualiService;
import it.usr.web.usrbilancio.service.OrdinativoService;
import it.usr.web.usrbilancio.service.QuietanzaService;
import it.usr.web.usrbilancio.service.SearchCriteria;
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
    List<DocumentoAllegato> allegati;
    DocumentoAllegato allegato;
    String testo;
    LocalDate dataDocDa;
    LocalDate dataDocA;
    LocalDate dataPagDa;
    LocalDate dataPagA;
    BigDecimal importoDa;
    BigDecimal importoA;
    Integer annoCompetenza;
    boolean dataDocAnd;
    boolean dataPagAnd;
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
        dataDocDa = null;
        dataDocA = null;
        dataPagDa = null;
        dataPagA = null;
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
        allegati = null;
        allegato = null;
        importoAnd = true;        
        tipiRtsAnd = true;
        codiciAnd = true;
        dataDocAnd = true;
        dataPagAnd = true;
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

    public List<DocumentoAllegato> getAllegati() {
        return allegati;
    }

    public DocumentoAllegato getAllegato() {
        return allegato;
    }

    public void setAllegato(DocumentoAllegato allegato) {
        this.allegato = allegato;
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

    public LocalDate getDataDocDa() {
        return dataDocDa;
    }

    public void setDataDocDa(LocalDate dataDocDa) {
        this.dataDocDa = dataDocDa;
    }

    public LocalDate getDataDocA() {
        return dataDocA;
    }

    public void setDataDocA(LocalDate dataDocA) {
        this.dataDocA = dataDocA;
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

    public boolean isDataDocAnd() {
        return dataDocAnd;
    }

    public void setDataDocAnd(boolean dataDocAnd) {
        this.dataDocAnd = dataDocAnd;
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

    public LocalDate getDataPagDa() {
        return dataPagDa;
    }

    public void setDataPagDa(LocalDate dataPagDa) {
        this.dataPagDa = dataPagDa;
    }

    public LocalDate getDataPagA() {
        return dataPagA;
    }

    public void setDataPagA(LocalDate dataPagA) {
        this.dataPagA = dataPagA;
    }

    public boolean isDataPagAnd() {
        return dataPagAnd;
    }

    public void setDataPagAnd(boolean dataPagAnd) {
        this.dataPagAnd = dataPagAnd;
    }
             
    public void cerca() {
        allegati = null;
        allegato = null;
        
        completato = false;
        List<String> tipSel = Arrays.asList(tipologieSelezionate!=null ? tipologieSelezionate : new String[]{});
        
        if(dataDocDa!=null && dataDocA!=null && dataDocDa.isAfter(dataDocA)) {
            LocalDate d = dataDocDa;
            dataDocDa = dataDocA;
            dataDocA = d;
        }
        
        if(dataPagDa!=null && dataPagA!=null && dataPagDa.isAfter(dataPagA)) {
            LocalDate d = dataPagDa;
            dataPagDa = dataPagA;
            dataPagA = d;
        }
        
        if(importoDa!=null && importoA!=null && importoDa.compareTo(importoA)>0) {
            BigDecimal i = importoDa;
            importoDa = importoA;
            importoA = i;
        }
        
        SearchCriteria sc = new SearchCriteria();
        sc.setTesto(testo);
        sc.setDataDocAnd(dataDocAnd);
        sc.setDataDocDa(dataDocDa);
        sc.setDataDocA(dataDocA);
        sc.setDataPagAnd(dataPagAnd);
        sc.setDataPagDa(dataPagDa);
        sc.setDataPagA(dataPagA);
        sc.setImportoAnd(importoAnd);
        sc.setImportoDa(importoDa);
        sc.setImportoA(importoA);
        sc.setTipiRts(tipiRtsSelezionati);
        sc.setCodiciAnd(codiciAnd);
        sc.setCodici(codiciSelezionati);
        sc.setAnnoCompAnd(annoCompAnd);
        sc.setAnnoCompetenza(annoCompetenza);
        sc.setCompetenze(competenzeSelezionate);
        
        List<OrdinativoRecord> ordinativi = tipSel.contains("O") ? os.cerca(sc) : new ArrayList();
        List<QuietanzaRecord> quietanze = tipSel.contains("Q") ? qs.cerca(sc) : new ArrayList();
        List<MovimentiVirtualiRecord> movimenti = tipSel.contains("MV") ? mvs.cerca(sc) : new ArrayList();
         
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
    
    public void aggiornaAllegati(RisultatoRicerca r) {
        allegati = new ArrayList<>();
        switch(r.getTipologia()) {
            case MOVIMENTO_VIRTUALE ->  {
                MovimentiVirtualiRecord mv = mvs.getMovimentoVirtualeById(r.getId());
                DocumentoAllegato da = new DocumentoAllegato(r.getId(), "MV", mv.getNomefile(), null, "Documento");
                allegati.add(da);
            }
            case ORDINATIVO ->  {
                List<AllegatoRecord> al = os.getAllegatiOrdinativo(r.getId());
                al.forEach(a -> {
                    DocumentoAllegato da = new DocumentoAllegato(a.getId(), "O", a.getNomefile(), a.getGruppo(), a.getDescrizione());
                    allegati.add(da);
                });
            }
            case QUIETANZA ->  {
                QuietanzaRecord qr = qs.getQuietanzaById(r.getId());
                DocumentoAllegato da = new DocumentoAllegato(r.getId(), "Q", qr.getNomefile(), null, "Documento");
                allegati.add(da);
            }
        }
    }
    
    private String formatBigDecimal(BigDecimal bd) {
        return bd==null ? "--" : new DecimalFormat("#,##0.00").format(bd);
    }
     
    public String formattaConto(CodiceRecord c) {
        StringBuilder sb = new StringBuilder();
        
        
        sb.append(truncate(c.getDescrizione(), 80));
        if(!isEmpty(c.getEnteDiocesi())) {
            sb.append(" [").append(c.getEnteDiocesi()).append("]");
        }
        
        if(!isEmpty(c.getProvincia())) {
            sb.append(" (").append(c.getProvincia()).append(")");
        }
        
        if(!isEmpty(c.getIdIntervento())) {
            sb.append(" [ID: ").append(c.getIdIntervento()).append("]");
        }
        
        if(!isEmpty(c.getOrdinanza())) {
            sb.append(" [").append(c.getOrdinanza()).append("]");
        } 
         
        return sb.toString();
    }
    
    public String truncate(String s, int len) {
        if(s==null) return null;
        
        return (s.length()>len) ? s.substring(0, len)+"..." : s;
    }
} 
