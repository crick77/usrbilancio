/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author riccardo.iovenitti
 */
public class SearchCriteria {
    String testo; 
    boolean dataDocAnd;
    LocalDate dataDocDa; 
    LocalDate dataDocA;
    boolean dataPagAnd;
    LocalDate dataPagDa; 
    LocalDate dataPagA;
    boolean importoAnd; 
    BigDecimal importoDa; 
    BigDecimal importoA;
    boolean tipiRtsAnd; 
    TipoRtsRecord[] tipiRts; 
    boolean codiciAnd; 
    CodiceRecord[] codici; 
    boolean annoCompAnd; 
    Integer annoCompetenza; 
    boolean competenzeAnd; 
    CapitoloCompetenza[] competenze;

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public boolean isDataDocAnd() {
        return dataDocAnd;
    }

    public void setDataDocAnd(boolean dataDocAnd) {
        this.dataDocAnd = dataDocAnd;
    }

    public LocalDate getDataDocDa() {
        return dataDocDa;
    }

    public void setDataDocDa(LocalDate dataDocDa) {
        this.dataDocDa = dataDocDa;
    }

    public LocalDate getDataDocA() {
        return dataDocA;
    }

    public void setDataDocA(LocalDate dataDocA) {
        this.dataDocA = dataDocA;
    }

    public boolean isDataPagAnd() {
        return dataPagAnd;
    }

    public void setDataPagAnd(boolean dataPagAnd) {
        this.dataPagAnd = dataPagAnd;
    }

    public LocalDate getDataPagDa() {
        return dataPagDa;
    }

    public void setDataPagDa(LocalDate dataPagDa) {
        this.dataPagDa = dataPagDa;
    }

    public LocalDate getDataPagA() {
        return dataPagA;
    }

    public void setDataPagA(LocalDate dataPagA) {
        this.dataPagA = dataPagA;
    }

    public boolean isImportoAnd() {
        return importoAnd;
    }

    public void setImportoAnd(boolean importoAnd) {
        this.importoAnd = importoAnd;
    }

    public BigDecimal getImportoDa() {
        return importoDa;
    }

    public void setImportoDa(BigDecimal importoDa) {
        this.importoDa = importoDa;
    }

    public BigDecimal getImportoA() {
        return importoA;
    }

    public void setImportoA(BigDecimal importoA) {
        this.importoA = importoA;
    }

    public boolean isTipiRtsAnd() {
        return tipiRtsAnd;
    }

    public void setTipiRtsAnd(boolean tipiRtsAnd) {
        this.tipiRtsAnd = tipiRtsAnd;
    }

    public TipoRtsRecord[] getTipiRts() {
        return tipiRts;
    }

    public void setTipiRts(TipoRtsRecord[] tipiRts) {
        this.tipiRts = tipiRts;
    }

    public boolean isCodiciAnd() {
        return codiciAnd;
    }

    public void setCodiciAnd(boolean codiciAnd) {
        this.codiciAnd = codiciAnd;
    }

    public CodiceRecord[] getCodici() {
        return codici;
    }

    public void setCodici(CodiceRecord[] codici) {
        this.codici = codici;
    }

    public boolean isAnnoCompAnd() {
        return annoCompAnd;
    }

    public void setAnnoCompAnd(boolean annoCompAnd) {
        this.annoCompAnd = annoCompAnd;
    }

    public Integer getAnnoCompetenza() {
        return annoCompetenza;
    }

    public void setAnnoCompetenza(Integer annoCompetenza) {
        this.annoCompetenza = annoCompetenza;
    }

    public boolean isCompetenzeAnd() {
        return competenzeAnd;
    }

    public void setCompetenzeAnd(boolean competenzeAnd) {
        this.competenzeAnd = competenzeAnd;
    }

    public CapitoloCompetenza[] getCompetenze() {
        return competenze;
    }

    public void setCompetenze(CapitoloCompetenza[] competenze) {
        this.competenze = competenze;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SearchCriteria{");
        sb.append("testo=").append(testo);
        sb.append(", dataDocAnd=").append(dataDocAnd);
        sb.append(", dataDocDa=").append(dataDocDa);
        sb.append(", dataDocA=").append(dataDocA);
        sb.append(", dataPagAnd=").append(dataPagAnd);
        sb.append(", dataPagDa=").append(dataPagDa);
        sb.append(", dataPagA=").append(dataPagA);
        sb.append(", importoAnd=").append(importoAnd);
        sb.append(", importoDa=").append(importoDa);
        sb.append(", importoA=").append(importoA);
        sb.append(", tipiRtsAnd=").append(tipiRtsAnd);
        sb.append(", tipiRts=").append(tipiRts);
        sb.append(", codiciAnd=").append(codiciAnd);
        sb.append(", codici=").append(codici);
        sb.append(", annoCompAnd=").append(annoCompAnd);
        sb.append(", annoCompetenza=").append(annoCompetenza);
        sb.append(", competenzeAnd=").append(competenzeAnd);
        sb.append(", competenze=").append(competenze);
        sb.append('}');
        return sb.toString();
    }        
}
