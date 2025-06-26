/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.pdfextract.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author riccardo.iovenitti
 */
public class QuietanzaGeocos {
    private Date dataContabile;
    private String numeroQuietanza;
    private BigDecimal importo;
    private String versante;
    private String causale;

    public Date getDataContabile() {
        return dataContabile;
    }

    public void setDataContabile(Date dataContabile) {
        this.dataContabile = dataContabile;
    }

    public String getNumeroQuietanza() {
        return numeroQuietanza;
    }

    public void setNumeroQuietanza(String numeroQuietanza) {
        this.numeroQuietanza = numeroQuietanza;
    }

    public BigDecimal getImporto() {
        return importo;
    }

    public void setImporto(BigDecimal importo) {
        this.importo = importo;
    }

    public String getVersante() {
        return versante;
    }

    public void setVersante(String versante) {
        this.versante = versante;
    }

    public String getCausale() {
        return causale;
    }

    public void setCausale(String causale) {
        this.causale = causale;
    }

    @Override
    public String toString() {
        return "DettaglioQuietanza{" + "dataContabile=" + dataContabile + ", numeroQuietanza=" + numeroQuietanza + ", importo=" + importo + ", versante=" + versante + ", causale=" + causale + '}';
    }        
}
