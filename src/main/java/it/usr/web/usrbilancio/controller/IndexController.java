/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.UtenteRecord;
import it.usr.web.usrbilancio.model.StatoCapitolo;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.OrdinativoService;
import it.usr.web.usrbilancio.service.QuietanzaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.primefaces.PrimeFaces;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.FilterMeta;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class IndexController extends BaseController {
    @Inject
    CompetenzaService compServ;
    @Inject
    OrdinativoService os;
    @Inject
    QuietanzaService qs;
    @Inject
    @AppLogger
    Logger logger;    
    List<StatoCapitolo> statoCapitoli;
    List<StatoCapitolo> statoCapitoliFiltrato;
    List<StatoCapitolo> statoCapitoliAnnoCorrente;    
    StatoCapitolo capitoloSelezionato;
    StatoCapitolo capitoloAnnoCorrenteSelezionato;
    List<Integer> anni;
    List<String> capitoli;
    List<FilterMeta> filterBy;
    BigDecimal saldo;
    BigDecimal saldoLR8;
    BigDecimal ivaPagata;
    BigDecimal ivaDaPagare;
    BigDecimal ivaAnagrafica;
    BigDecimal totaleQuietanze;
    BigDecimal totaleOrdinativi;
    int numeroOrdinativiMesePrec;
    int numeroOrdinativiMeseCorr;
    Integer ultimoNumeroOrdinativo;
    Map<String, BigDecimal> totaliAnnoCorrente;
    Map<String, BigDecimal> totaliComplessivo;
    String[] selectedCapitoli;
    Integer[] selectedAnni;
    LocalDate dataSaldo;
    LocalDate dataOrdQui;
    
    public String init() {       
        UtenteRecord u = (UtenteRecord)getUtente().getAttributes();
        if(u.getPubblica()==1) return "pubblica/index";
        
        dataSaldo = LocalDate.now();
        dataOrdQui = dataSaldo;
        totaliAnnoCorrente = new HashMap<>();
        totaliAnnoCorrente.put("Q", BigDecimal.ZERO);
        totaliAnnoCorrente.put("O", BigDecimal.ZERO);
        totaliAnnoCorrente.put("S", BigDecimal.ZERO);
        totaliAnnoCorrente.put("V", BigDecimal.ZERO);
        totaliAnnoCorrente.put("SV", BigDecimal.ZERO);
        totaliComplessivo = new HashMap<>();
        totaliComplessivo.putAll(totaliAnnoCorrente);
        
        aggiornaAnnoCorrente();
        aggiornaSaldo();
        aggiornaSaldoLR8();
        aggiornaOrdinativiMeseCorrente();
        aggiornaOrdinativiMesePrecedente();
        aggiornaUltimoNumeroOrdinativo();
        aggiornaIVA();
        aggiornaTotaliQuietanzeOrdintivi();
        
        statoCapitoliFiltrato = null;
        capitoloSelezionato = null;
        capitoloAnnoCorrenteSelezionato = null;
        filterBy = new ArrayList<>();
        selectedCapitoli = null;
        selectedAnni = null;
        
        return SAME_VIEW;
    }

    public BigDecimal getTotaleQuietanze() {
        return totaleQuietanze;
    }

    public BigDecimal getTotaleOrdinativi() {
        return totaleOrdinativi;
    }
        
    public LocalDate getDataOrdQui() {
        return dataOrdQui;
    }

    public void setDataOrdQui(LocalDate dataOrdQui) {
        this.dataOrdQui = dataOrdQui;
    }
        
    public LocalDate getDataSaldo() {
        return dataSaldo;
    }

    public void setDataSaldo(LocalDate dataSaldo) { 
        this.dataSaldo = dataSaldo!=null ? dataSaldo : getToday();
    }
        
    public String[] getSelectedCapitoli() {
        return selectedCapitoli;
    }

    public void setSelectedCapitoli(String[] selectedCapitoli) {
        this.selectedCapitoli = selectedCapitoli;       
    }

    public Integer[] getSelectedAnni() {
        return selectedAnni;
    }

    public void setSelectedAnni(Integer[] selectedAnni) {
        this.selectedAnni = selectedAnni;
    }
                
    public Map<String, BigDecimal> getTotaliAnnoCorrente() {
        return totaliAnnoCorrente;
    }

    public Map<String, BigDecimal> getTotaliComplessivo() {
        return totaliComplessivo;
    }
        
    public List<StatoCapitolo> getStatoCapitoli() {
        return statoCapitoli;
    }

    public List<StatoCapitolo> getStatoCapitoliFiltrato() {
        return statoCapitoliFiltrato;
    }

    public void setStatoCapitoliFiltrato(List<StatoCapitolo> statoCapitoliFiltrato) {
        this.statoCapitoliFiltrato = statoCapitoliFiltrato;
    }    

    public List<Integer> getAnni() {
        return anni;
    }

    public List<String> getCapitoli() {
        return capitoli;
    }
        
    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }
        
    public StatoCapitolo getCapitoloSelezionato() {
        return capitoloSelezionato;
    }

    public void setCapitoloSelezionato(StatoCapitolo capitoloSelezionato) {
        this.capitoloSelezionato = capitoloSelezionato;
    }

    public List<StatoCapitolo> getStatoCapitoliAnnoCorrente() {
        return statoCapitoliAnnoCorrente;
    }

    public StatoCapitolo getCapitoloAnnoCorrenteSelezionato() {
        return capitoloAnnoCorrenteSelezionato;
    }

    public void setCapitoloAnnoCorrenteSelezionato(StatoCapitolo capitoloAnnoCorrenteSelezionato) {
        this.capitoloAnnoCorrenteSelezionato = capitoloAnnoCorrenteSelezionato;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public BigDecimal getSaldoLR8() {
        return saldoLR8;
    }

    public int getNumeroOrdinativiMesePrec() {
        return numeroOrdinativiMesePrec;
    }

    public int getNumeroOrdinativiMeseCorr() {
        return numeroOrdinativiMeseCorr;
    }

    public Integer getUltimoNumeroOrdinativo() {
        return ultimoNumeroOrdinativo;
    }

    public BigDecimal getIvaPagata() {
        return ivaPagata;
    }

    public BigDecimal getIvaDaPagare() {
        return ivaDaPagare;
    }

    public BigDecimal getIvaAnagrafica() {
        return ivaAnagrafica;
    }
                      
    public String getFormat(BigDecimal i) {
        if(i!=null) {
            int res = i.compareTo(BigDecimal.ZERO);
            return res==-1 ?
                   "red" :
                   (res>0 ? "green" : "");
        }
        
        return "";
    }        
    
    public void aggiornaAnnoCorrente() {
        //statoCapitoliAnnoCorrente = compServ.getSituazione(getAnnoAttuale());
        statoCapitoliAnnoCorrente = compServ.getSituazioneAperti();
        aggiornaTotaliAnnoCorrente();
        capitoloAnnoCorrenteSelezionato = null;
        
        statoCapitoli = null;
        anni = null;
        capitoli = null;
        statoCapitoliFiltrato = null;
        capitoloSelezionato = null;                
    }
    
    public void aggiornaStatoCapitoli() {
        statoCapitoli = compServ.getSituazione(null);
        statoCapitoliFiltrato = statoCapitoli;
        aggiornaTotaliComplessivi();
        
        anni = new ArrayList<>();
        capitoli = new ArrayList<>();
        statoCapitoli.forEach(sc -> {
            if(!anni.contains(sc.getAnno())) anni.add(sc.getAnno());
            if(!capitoli.contains(sc.getDescrizione())) capitoli.add(sc.getDescrizione());
        });
        
        statoCapitoliAnnoCorrente = null;
        capitoloAnnoCorrenteSelezionato = null;
        
        clearFilters(false);
    }
    
    public void aggiornaSaldo() {
        saldo = compServ.getSaldoGeocos(dataSaldo);
    }
    
    public void aggiornaSaldoLR8() {
        saldoLR8 = compServ.getSaldoLR8();
    }
    
    public void aggiornaOrdinativiMesePrecedente() {
        LocalDate initial = LocalDate.now().minusMonths(1);
        LocalDate start = initial.withDayOfMonth(1);
        LocalDate end = initial.withDayOfMonth(initial.getMonth().length(initial.isLeapYear()));
        numeroOrdinativiMesePrec = os.getNumeroOrdinativiPeriodo(start, end);
    }
    
    public void aggiornaOrdinativiMeseCorrente() {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);        
        numeroOrdinativiMeseCorr = os.getNumeroOrdinativiPeriodo(start, now);
    }
    
    public void aggiornaUltimoNumeroOrdinativo() {
        ultimoNumeroOrdinativo = os.getUltimoNumeroOrdinativo(getAnnoAttuale());
    }
    
    public void aggiornaTotaliQuietanzeOrdintivi() {
        //LocalDate min = dataOrdQui.withMonth(1).withDayOfMonth(1);
        LocalDate min = dataOrdQui;
        BigDecimal to = os.getTotaleOrdinativiPeriodo(min, dataOrdQui);
        BigDecimal tq = qs.getTotaleQuietanzePeriodo(min, dataOrdQui);
        totaleOrdinativi = (to!=null) ? to : BigDecimal.ZERO;
        totaleQuietanze = (tq!=null) ? tq : BigDecimal.ZERO;
    }
    
    public void aggiornaIVA() {
        ivaDaPagare = os.getImportoIVADaPagare(getAnnoAttuale());
        if(ivaDaPagare==null) ivaDaPagare = BigDecimal.ZERO;
        ivaPagata = os.getImportoIVAPagata(getAnnoAttuale());
        if(ivaPagata==null) ivaPagata = BigDecimal.ZERO;
        ivaAnagrafica = os.getImportoIVAAnagrafica(getAnnoAttuale());
        if(ivaAnagrafica==null) ivaAnagrafica = BigDecimal.ZERO;
    }
    
    public String getIVAOk() {
        BigDecimal res = ivaAnagrafica.subtract(ivaPagata).subtract(ivaDaPagare);
        logger.info("IVA ANAGRAFICA {}, IVA PAGATA {}, IVA DA PAGARE {}, RES {}.", ivaAnagrafica, ivaPagata, ivaDaPagare, res);
        return (res.compareTo(BigDecimal.ZERO)==0) ? "green" : "red";
    }
    
    public void onChange(TabChangeEvent event) {
        switch(event.getTab().getId().toUpperCase()) {
            case "ANNOCORRENTE" ->  {
                aggiornaAnnoCorrente();
            }
            case "TUTTIGLIANNI" ->  {
                aggiornaStatoCapitoli();
            }            
        }
    }       

    public void aggiornaTotaliAnnoCorrente() {        
        for(String k : totaliAnnoCorrente.keySet()) {
            totaliAnnoCorrente.put(k, BigDecimal.ZERO);
        }
        
        for(StatoCapitolo sc : statoCapitoliAnnoCorrente) {
            totaliAnnoCorrente.put("O", totaliAnnoCorrente.get("O").add(sc.getImportoOrdinativi()));
            totaliAnnoCorrente.put("Q", totaliAnnoCorrente.get("Q").add(sc.getImportoQuietanze()));
            totaliAnnoCorrente.put("S", totaliAnnoCorrente.get("S").add(sc.getSaldo()));
            totaliAnnoCorrente.put("V", totaliAnnoCorrente.get("V").add(sc.getImportoVirtuale()));
            totaliAnnoCorrente.put("SV", totaliAnnoCorrente.get("SV").add(sc.getSaldoVirtuale()));
        }
    }

    public boolean isSaldoLR8ChiusiDifferente() {
        return !saldoLR8.equals(compServ.getSaldoVirtualeChiusi());
    }
    
    public void aggiornaTotaliComplessivi() {
        for(String k : totaliComplessivo.keySet()) {
            totaliComplessivo.put(k, BigDecimal.ZERO);
        }
        
        if(isEmpty(statoCapitoliFiltrato)) return;
        
        for(StatoCapitolo sc : statoCapitoliFiltrato) {
            totaliComplessivo.put("O", totaliComplessivo.get("O").add(sc.getImportoOrdinativi()));
            totaliComplessivo.put("Q", totaliComplessivo.get("Q").add(sc.getImportoQuietanze()));
            totaliComplessivo.put("S", totaliComplessivo.get("S").add(sc.getSaldo()));
            totaliComplessivo.put("V", totaliComplessivo.get("V").add(sc.getImportoVirtuale()));
            totaliComplessivo.put("SV", totaliComplessivo.get("SV").add(sc.getSaldoVirtuale()));
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
    
    public boolean filterFunctionAnni(Object value, Object filter, Locale locale) {
        if(value==null || !(value instanceof Integer)) return true;
        int iVal = (Integer)value;
        if(selectedAnni==null || selectedAnni.length==0) return false;
        for(int sel : selectedAnni) {
            if(sel==iVal) return true;
        }
        
        return false;
    }
    
    public void clearFilters(boolean resetFilters) {
        selectedCapitoli = new String[statoCapitoli.size()];
        for(int i=0;i<statoCapitoli.size();i++) {
            selectedCapitoli[i] = statoCapitoli.get(i).getDescrizione();
        }
        
        selectedAnni = new Integer[anni.size()];
        for(int i=0;i<anni.size();i++) {
            selectedAnni[i] = anni.get(i);
        }
        
        filterBy = new ArrayList<>();
        
        if(resetFilters) {
            PrimeFaces.current().executeScript("PF('statoTable').clearFilters();PF('capcompcbm').checkAll();PF('annicbm').checkAll();");
        }
    }
}
