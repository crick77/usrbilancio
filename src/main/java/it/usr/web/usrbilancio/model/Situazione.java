/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author riccardo.iovenitti
 */
public class Situazione implements Serializable {
    private int idCompetenza;
    private String capitolo;
    private Integer anno;
    private Integer chiuso;
    private BigDecimal stanziamento;
    private BigDecimal finanziato;
    private BigDecimal speso;
    private BigDecimal preventivato;
    private BigDecimal richiesto;

    public Integer getChiuso() {
        return chiuso;
    }

    public void setChiuso(Integer chiuso) {
        this.chiuso = chiuso;
    }
        
    public int getIdCompetenza() {
        return idCompetenza;
    }

    public void setIdCompetenza(int idCompetenza) {
        this.idCompetenza = idCompetenza;
    }

    public String getCapitolo() {
        return capitolo;
    }

    public void setCapitolo(String capitolo) {
        this.capitolo = capitolo;
    }

    public Integer getAnno() {
        return anno;
    }

    public void setAnno(Integer anno) {
        this.anno = anno;
    }

    public BigDecimal getStanziamento() {
        return stanziamento;
    }

    public void setStanziamento(BigDecimal stanziamento) {
        this.stanziamento = stanziamento;
    }

    public BigDecimal getFinanziato() {
        return finanziato;
    }

    public void setFinanziato(BigDecimal finanziato) {
        this.finanziato = finanziato;
    }

    public BigDecimal getSpeso() {
        return speso;
    }

    public void setSpeso(BigDecimal speso) {
        this.speso = speso;
    }

    public BigDecimal getRichiesto() {
        return richiesto;
    }

    public void setRichiesto(BigDecimal richiesto) {
        this.richiesto = richiesto;
    }

    public BigDecimal getPreventivato() {
        return preventivato;
    }

    public void setPreventivato(BigDecimal preventivato) {
        this.preventivato = preventivato;
    }     
    
    public String getTitolo() {
        StringBuilder sb = new StringBuilder();
        
        if(anno!=null) {
            sb = sb.append(anno);        
            if(capitolo!=null) sb = sb.append(" | ");            
        }        
        if(capitolo!=null) {
            sb.append(capitolo);
        }
        
        return sb.toString();
    }
}
