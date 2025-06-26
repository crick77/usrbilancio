/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 *
 * @author riccardo.iovenitti
 */
public class StatoCapitolo implements Serializable {
    private int id;
    private String descrizione;
    private int anno;
    private byte chiuso;
    private BigDecimal importoOrdinativi;
    private BigDecimal importoQuietanze;
    private BigDecimal importoVirtuale;

    public StatoCapitolo() {
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

    public BigDecimal getImportoOrdinativi() {
        return importoOrdinativi;
    }

    public void setImportoOrdinativi(BigDecimal importoOrdinativi) {
        this.importoOrdinativi = importoOrdinativi;
    }

    public BigDecimal getImportoQuietanze() {
        return importoQuietanze;
    }

    public void setImportoQuietanze(BigDecimal importoQuietanze) {
        this.importoQuietanze = importoQuietanze;
    }

    public BigDecimal getImportoVirtuale() {
        return importoVirtuale;
    }

    public void setImportoVirtuale(BigDecimal importoVirtuale) {
        this.importoVirtuale = importoVirtuale;
    }

    public BigDecimal getSaldo() {
        return importoQuietanze.subtract(importoOrdinativi);
    }
    
    public BigDecimal getSaldoVirtuale() {
        return importoQuietanze.subtract(importoOrdinativi).add(importoVirtuale);
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + this.id;
        hash = 17 * hash + Objects.hashCode(this.descrizione);
        hash = 17 * hash + this.anno;
        hash = 17 * hash + this.chiuso;
        hash = 17 * hash + Objects.hashCode(this.importoOrdinativi);
        hash = 17 * hash + Objects.hashCode(this.importoQuietanze);
        hash = 17 * hash + Objects.hashCode(this.importoVirtuale);
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
        final StatoCapitolo other = (StatoCapitolo) obj;
        return this.id == other.id;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("StatoCapitolo{");
        sb.append("id=").append(id);
        sb.append(", descrizione=").append(descrizione);
        sb.append(", anno=").append(anno);
        sb.append(", chiuso=").append(chiuso);
        sb.append(", importoOrdinativi=").append(importoOrdinativi);
        sb.append(", importoQuietanze=").append(importoQuietanze);
        sb.append(", importoVirtuale=").append(importoVirtuale);
        sb.append('}');
        return sb.toString();
    }          
}
