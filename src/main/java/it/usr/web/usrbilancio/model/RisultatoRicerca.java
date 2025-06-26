/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author riccardo.iovenitti
 */
public class RisultatoRicerca implements Serializable {
    private int id;
    private Integer idCompetenza;
    private Integer idTipoDocumento;
    private Integer idTipoRts;
    private Integer idCodice;
    private String numeroPagamento;
    private LocalDate dataPagamento;
    private String numeroDocumento;
    private LocalDate dataDocumento;
    private LocalDate dataRicevimento;
    private String soggetto;
    private String descrizione;
    private BigDecimal importoE;
    private BigDecimal importoU;
    private BigDecimal importoV;
    private String note;
    private TipoRisultato tipologia; 

    public RisultatoRicerca(TipoRisultato tipologia) {
        this.tipologia = tipologia;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getIdCompetenza() {
        return idCompetenza;
    }

    public void setIdCompetenza(Integer idCompetenza) {
        this.idCompetenza = idCompetenza;
    }

    public Integer getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(Integer idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public Integer getIdTipoRts() {
        return idTipoRts;
    }

    public void setIdTipoRts(Integer idTipoRts) {
        this.idTipoRts = idTipoRts;
    }

    public Integer getIdCodice() {
        return idCodice;
    }

    public void setIdCodice(Integer idCodice) {
        this.idCodice = idCodice;
    }

    public String getNumeroPagamento() {
        return numeroPagamento;
    }

    public void setNumeroPagamento(String numeroPagamento) {
        this.numeroPagamento = numeroPagamento;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public LocalDate getDataDocumento() {
        return dataDocumento;
    }

    public void setDataDocumento(LocalDate dataDocumento) {
        this.dataDocumento = dataDocumento;
    }

    public LocalDate getDataRicevimento() {
        return dataRicevimento;
    }

    public void setDataRicevimento(LocalDate dataRicevimento) {
        this.dataRicevimento = dataRicevimento;
    }

    public String getSoggetto() {
        return soggetto;
    }

    public void setSoggetto(String soggetto) {
        this.soggetto = soggetto;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public BigDecimal getImportoE() {
        return importoE;
    }

    public void setImportoE(BigDecimal importoE) {
        this.importoE = importoE;
    }

    public BigDecimal getImportoU() {
        return importoU;
    }

    public void setImportoU(BigDecimal importoU) {
        this.importoU = importoU;
    }

    public BigDecimal getImportoV() {
        return importoV;
    }

    public void setImportoV(BigDecimal importoV) {
        this.importoV = importoV;
    }
    
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public TipoRisultato getTipologia() {
        return tipologia;
    }

    public void setTipologia(TipoRisultato tipologia) {
        this.tipologia = tipologia;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.id;
        hash = 53 * hash + Objects.hashCode(this.idCompetenza);
        hash = 53 * hash + Objects.hashCode(this.idTipoDocumento);
        hash = 53 * hash + Objects.hashCode(this.idTipoRts);
        hash = 53 * hash + Objects.hashCode(this.idCodice);
        hash = 53 * hash + Objects.hashCode(this.numeroPagamento);
        hash = 53 * hash + Objects.hashCode(this.dataPagamento);
        hash = 53 * hash + Objects.hashCode(this.numeroDocumento);
        hash = 53 * hash + Objects.hashCode(this.dataDocumento);
        hash = 53 * hash + Objects.hashCode(this.dataRicevimento);
        hash = 53 * hash + Objects.hashCode(this.soggetto);
        hash = 53 * hash + Objects.hashCode(this.descrizione);
        hash = 53 * hash + Objects.hashCode(this.importoE);
        hash = 53 * hash + Objects.hashCode(this.importoU);
        hash = 53 * hash + Objects.hashCode(this.importoV);
        hash = 53 * hash + Objects.hashCode(this.note);
        hash = 53 * hash + Objects.hashCode(this.tipologia);
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
        final RisultatoRicerca other = (RisultatoRicerca) obj;
        if (this.id != other.id) {
            return false;
        }
        return this.tipologia == other.tipologia;
    }
            
    public enum TipoRisultato {
        ORDINATIVO, QUIETANZA, MOVIMENTO_VIRTUALE;
    }       
}
