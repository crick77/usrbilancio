/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 *
 * @author riccardo.iovenitti
 */
public class CapitoloCompetenza {
    private int id;
    private int idCapitolo;
    private String descrizione;
    private BigDecimal stanziamento;
    private int anno;
    private byte chiuso;
    private byte nuovoanno;
    private byte daconsolidare;

    public CapitoloCompetenza() {
    }

    public CapitoloCompetenza(int id, int idCapitolo, String descrizione, BigDecimal stanziamento, int anno, byte chiuso, byte nuovoanno, byte daconsolidare) {
        this.id = id;
        this.idCapitolo = idCapitolo;
        this.descrizione = descrizione;
        this.stanziamento = stanziamento;
        this.anno = anno;
        this.chiuso = chiuso;
        this.nuovoanno = nuovoanno;
        this.daconsolidare = daconsolidare;
    }
        
    public int getIdCapitolo() {
        return idCapitolo;
    }

    public void setIdCapitolo(int idCapitolo) {
        this.idCapitolo = idCapitolo;
    }               
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public BigDecimal getStanziamento() {
        return stanziamento;
    }

    public void setStanziamento(BigDecimal stanziamento) {
        this.stanziamento = stanziamento;
    }
        
    public int getAnno() {
        return anno;
    }

    public void setAnno(int anno) {
        this.anno = anno;
    }

    public byte getChiuso() {
        return chiuso;
    }

    public void setChiuso(byte chiuso) {
        this.chiuso = chiuso;
    }  

    public byte getNuovoanno() {
        return nuovoanno;
    }

    public void setNuovoanno(byte nuovoanno) {
        this.nuovoanno = nuovoanno;
    }

    public byte getDaconsolidare() {
        return daconsolidare;
    }

    public void setDaconsolidare(byte daconsolidare) {
        this.daconsolidare = daconsolidare;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.id;
        hash = 97 * hash + this.idCapitolo;
        hash = 97 * hash + Objects.hashCode(this.descrizione);
        hash = 97 * hash + Objects.hashCode(this.stanziamento);
        hash = 97 * hash + this.anno;
        hash = 97 * hash + this.chiuso;
        hash = 97 * hash + this.nuovoanno;
        hash = 97 * hash + this.daconsolidare;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CapitoloCompetenza other = (CapitoloCompetenza) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CapitoloCompetenza{");
        sb.append("id=").append(id);
        sb.append(", idCapitolo=").append(idCapitolo);
        sb.append(", descrizione=").append(descrizione);
        sb.append(", stanziamento=").append(stanziamento);
        sb.append(", anno=").append(anno);
        sb.append(", chiuso=").append(chiuso);
        sb.append(", nuovoanno=").append(nuovoanno);
        sb.append(", daconsolidare=").append(daconsolidare);
        sb.append('}');
        return sb.toString();
    }   
}
