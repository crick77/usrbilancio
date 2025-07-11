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
public class Intervento {
    private int id;
    private String codice;
    private String c01;
    private String c02;
    private String c03;
    private String c04;
    private String c05;
    private String descrizione;
    private String enteDiocesi;
    private String idIntervento;
    private BigDecimal importoFinanziamento;
    private BigDecimal importoCoFinanziamento;
    private BigDecimal importoContoTermico;
    private String note;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public String getC01() {
        return c01;
    }

    public void setC01(String c01) {
        this.c01 = c01;
    }

    public String getC02() {
        return c02;
    }

    public void setC02(String c02) {
        this.c02 = c02;
    }

    public String getC03() {
        return c03;
    }

    public void setC03(String c03) {
        this.c03 = c03;
    }

    public String getC04() {
        return c04;
    }

    public void setC04(String c04) {
        this.c04 = c04;
    }

    public String getC05() {
        return c05;
    }

    public void setC05(String c05) {
        this.c05 = c05;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getEnteDiocesi() {
        return enteDiocesi;
    }

    public void setEnteDiocesi(String enteDiocesi) {
        this.enteDiocesi = enteDiocesi;
    }

    public String getIdIntervento() {
        return idIntervento;
    }

    public void setIdIntervento(String idIntervento) {
        this.idIntervento = idIntervento;
    }

    public BigDecimal getImportoFinanziamento() {
        return importoFinanziamento;
    }

    public void setImportoFinanziamento(BigDecimal importoFinanziamento) {
        this.importoFinanziamento = importoFinanziamento;
    }

    public BigDecimal getImportoCoFinanziamento() {
        return importoCoFinanziamento;
    }

    public void setImportoCoFinanziamento(BigDecimal importoCoFinanziamento) {
        this.importoCoFinanziamento = importoCoFinanziamento;
    }

    public BigDecimal getImportoContoTermico() {
        return importoContoTermico;
    }

    public void setImportoContoTermico(BigDecimal importoContoTermico) {
        this.importoContoTermico = importoContoTermico;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    
    
}
