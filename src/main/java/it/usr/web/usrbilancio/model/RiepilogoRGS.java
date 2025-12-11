/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author riccardo.iovenitti
 */
public class RiepilogoRGS {
    RiepilogoEntrate entrate;
    RiepilogoUscite uscite;
    int esercizioFinanziario;
    int rendicontoNr;
    LocalDate dal;
    LocalDate al;
    LocalDate dataElaborazione;

    public RiepilogoRGS(int esercizioFinanziario, int rendicontoNr, LocalDate dal, LocalDate al) {
        this.entrate = new RiepilogoEntrate();
        this.uscite = new RiepilogoUscite();
        this.esercizioFinanziario = esercizioFinanziario;
        this.rendicontoNr = rendicontoNr;
        this.dal = dal;
        this.al = al;
        this.dataElaborazione = LocalDate.now();
    }
        
    public RiepilogoEntrate getEntrate() {
        return entrate;
    }

    public RiepilogoUscite getUscite() {
        return uscite;
    }

    public int getEsercizioFinanziario() {
        return esercizioFinanziario;
    }

    public int getRendicontoNr() {
        return rendicontoNr;
    }

    public LocalDate getDal() {
        return dal;
    }

    public LocalDate getAl() {
        return al;
    }

    public LocalDate getDataElaborazione() {
        return dataElaborazione;
    }      
    
    public BigDecimal getTotaleEntrate() {
        BigDecimal tot = BigDecimal.ZERO;
        tot = tot.add(entrate.getGiacenzaCassaInizioAnno());
        tot = tot.add(entrate.getFondiComunitari());
        tot = tot.add(entrate.getFondiStatali());
        tot = tot.add(entrate.getFondiRegionali());
        tot = tot.add(entrate.getFondiEntiLocali());
        tot = tot.add(entrate.getTariffeServizi());
        tot = tot.add(entrate.getAccensionePrestiti());
        tot = tot.add(entrate.getAltro());
        
        return tot;
    }
    
    public BigDecimal getTotaleUscite() {
        BigDecimal tot = BigDecimal.ZERO;
        tot = tot.add(uscite.getRedditiDaLD());
        tot = tot.add(uscite.getConsumiItermedi());
        tot = tot.add(uscite.getInteressiPassivi());
        tot = tot.add(uscite.getTrasferimentiARegioni());
        tot = tot.add(uscite.getTrasferimentiaEL());
        tot = tot.add(uscite.getInvestimentiDiretti());
        tot = tot.add(uscite.getTrasferimentiContoCapitale());
        tot = tot.add(uscite.getRimborsiPrestiti());
        tot = tot.add(uscite.getVersamentiErariali());
        tot = tot.add(uscite.getVersamentiPrevidenziali());
        tot = tot.add(uscite.getAltro());
        
        return tot;
    }
}
