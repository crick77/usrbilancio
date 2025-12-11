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
public class RiepilogoEntrate {
    BigDecimal giacenzaCassaInizioAnno;
    BigDecimal fondiComunitari; 
    BigDecimal fondiStatali;
    BigDecimal fondiRegionali;
    BigDecimal fondiEntiLocali;
    BigDecimal tariffeServizi;
    BigDecimal accensionePrestiti;
    BigDecimal altro;

    public RiepilogoEntrate() {
        this.giacenzaCassaInizioAnno = BigDecimal.ZERO;
        this.fondiComunitari = BigDecimal.ZERO;
        this.fondiStatali = BigDecimal.ZERO;
        this.fondiRegionali = BigDecimal.ZERO;
        this.fondiEntiLocali = BigDecimal.ZERO;
        this.tariffeServizi = BigDecimal.ZERO;
        this.accensionePrestiti = BigDecimal.ZERO;
        this.altro = BigDecimal.ZERO;
    }
        
    public BigDecimal getGiacenzaCassaInizioAnno() {
        return giacenzaCassaInizioAnno; 
    }

    public void setGiacenzaCassaInizioAnno(BigDecimal giacenzaCassaInizioAnno) {
        this.giacenzaCassaInizioAnno = giacenzaCassaInizioAnno;
    }

    public BigDecimal getFondiComunitari() {
        return fondiComunitari;
    }

    public void setFondiComunitari(BigDecimal fondiComunitari) {
        this.fondiComunitari = fondiComunitari;
    }

    public BigDecimal getFondiStatali() {
        return fondiStatali;
    }

    public void setFondiStatali(BigDecimal fondiStatali) {
        this.fondiStatali = fondiStatali;
    }

    public BigDecimal getFondiRegionali() {
        return fondiRegionali;
    }

    public void setFondiRegionali(BigDecimal fondiRegionali) {
        this.fondiRegionali = fondiRegionali;
    }

    public BigDecimal getFondiEntiLocali() {
        return fondiEntiLocali;
    }

    public void setFondiEntiLocali(BigDecimal fondiEntiLocali) {
        this.fondiEntiLocali = fondiEntiLocali;
    }

    public BigDecimal getTariffeServizi() {
        return tariffeServizi;
    }

    public void setTariffeServizi(BigDecimal tariffeServizi) {
        this.tariffeServizi = tariffeServizi;
    }

    public BigDecimal getAccensionePrestiti() {
        return accensionePrestiti;
    }

    public void setAccensionePrestiti(BigDecimal accensionePrestiti) {
        this.accensionePrestiti = accensionePrestiti;
    }

    public BigDecimal getAltro() {
        return altro;
    }

    public void setAltro(BigDecimal altro) {
        this.altro = altro;
    }
    
    
}
