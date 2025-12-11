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
public class OperaPubblica {
    private String codice;
    private String ordinanza;
    private String provincia;
    private String ente;
    private String intervento;
    private BigDecimal trasferito;
    private BigDecimal liquidato;
    private BigDecimal liquidatoAp;

    public OperaPubblica(String codice, String ordinanza, String provincia, String ente, String intervento, BigDecimal trasferito, BigDecimal liquidato, BigDecimal liquidatoAp) {
        this.codice = codice;
        this.ordinanza = ordinanza;
        this.provincia = provincia;
        this.ente = ente;
        this.intervento = intervento;
        this.trasferito = trasferito;
        this.liquidato = liquidato;
        this.liquidatoAp = liquidatoAp;
    }
    
    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }
            
    public String getOrdinanza() {
        return ordinanza;
    }

    public void setOrdinanza(String ordinanza) {
        this.ordinanza = ordinanza;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getEnte() {
        return ente;
    }

    public void setEnte(String ente) {
        this.ente = ente;
    }

    public String getIntervento() {
        return intervento;
    }

    public void setIntervento(String intervento) {
        this.intervento = intervento;
    }

    public BigDecimal getTrasferito() {
        return trasferito;
    }

    public void setTrasferito(BigDecimal trasferito) {
        this.trasferito = trasferito;
    }

    public BigDecimal getLiquidato() {
        return liquidato;
    }

    public void setLiquidato(BigDecimal liquidato) {
        this.liquidato = liquidato;
    }

    public BigDecimal getLiquidatoAp() {
        return liquidatoAp;
    }

    public void setLiquidatoAp(BigDecimal liquidatoAp) {
        this.liquidatoAp = liquidatoAp;
    }      
}
