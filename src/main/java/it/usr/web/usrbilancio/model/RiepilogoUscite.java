/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.model;

import java.math.BigDecimal;

/**
 *
 * @author riccardo.iovenitti
 */
public class RiepilogoUscite {
    BigDecimal redditiDaLD;
    BigDecimal consumiItermedi;
    BigDecimal interessiPassivi;
    BigDecimal trasferimentiARegioni;
    BigDecimal trasferimentiaEL;
    BigDecimal investimentiDiretti;
    BigDecimal trasferimentiContoCapitale;
    BigDecimal rimborsiPrestiti;
    BigDecimal versamentiErariali;
    BigDecimal versamentiPrevidenziali;
    BigDecimal altro; 

    public RiepilogoUscite() {
        this.redditiDaLD = BigDecimal.ZERO;
        this.consumiItermedi = BigDecimal.ZERO;
        this.interessiPassivi = BigDecimal.ZERO;
        this.trasferimentiARegioni = BigDecimal.ZERO;
        this.trasferimentiaEL = BigDecimal.ZERO;
        this.investimentiDiretti = BigDecimal.ZERO;
        this.trasferimentiContoCapitale = BigDecimal.ZERO;
        this.rimborsiPrestiti = BigDecimal.ZERO;
        this.versamentiErariali = BigDecimal.ZERO;
        this.versamentiPrevidenziali = BigDecimal.ZERO;
        this.altro = BigDecimal.ZERO;
    }
        
    public BigDecimal getRedditiDaLD() {
        return redditiDaLD;
    }

    public void setRedditiDaLD(BigDecimal redditiDaLD) {
        this.redditiDaLD = redditiDaLD;
    }

    public BigDecimal getConsumiItermedi() {
        return consumiItermedi;
    }

    public void setConsumiItermedi(BigDecimal consumiItermedi) {
        this.consumiItermedi = consumiItermedi;
    }

    public BigDecimal getInteressiPassivi() {
        return interessiPassivi;
    }

    public void setInteressiPassivi(BigDecimal interessiPassivi) {
        this.interessiPassivi = interessiPassivi;
    }

    public BigDecimal getTrasferimentiARegioni() {
        return trasferimentiARegioni;
    }

    public void setTrasferimentiARegioni(BigDecimal trasferimentiARegioni) {
        this.trasferimentiARegioni = trasferimentiARegioni;
    }

    public BigDecimal getTrasferimentiaEL() {
        return trasferimentiaEL;
    }

    public void setTrasferimentiaEL(BigDecimal trasferimentiaEL) {
        this.trasferimentiaEL = trasferimentiaEL;
    }

    public BigDecimal getInvestimentiDiretti() {
        return investimentiDiretti;
    }

    public void setInvestimentiDiretti(BigDecimal investimentiDiretti) {
        this.investimentiDiretti = investimentiDiretti;
    }

    public BigDecimal getTrasferimentiContoCapitale() {
        return trasferimentiContoCapitale;
    }

    public void setTrasferimentiContoCapitale(BigDecimal trasferimentiContoCapitale) {
        this.trasferimentiContoCapitale = trasferimentiContoCapitale;
    }

    public BigDecimal getRimborsiPrestiti() {
        return rimborsiPrestiti;
    }

    public void setRimborsiPrestiti(BigDecimal rimborsiPrestiti) {
        this.rimborsiPrestiti = rimborsiPrestiti;
    }

    public BigDecimal getVersamentiErariali() {
        return versamentiErariali;
    }

    public void setVersamentiErariali(BigDecimal versamentiErariali) {
        this.versamentiErariali = versamentiErariali;
    }

    public BigDecimal getVersamentiPrevidenziali() {
        return versamentiPrevidenziali;
    }

    public void setVersamentiPrevidenziali(BigDecimal versamentiPrevidenziali) {
        this.versamentiPrevidenziali = versamentiPrevidenziali;
    }

    public BigDecimal getAltro() {
        return altro;
    }

    public void setAltro(BigDecimal altro) {
        this.altro = altro;
    }       
}
