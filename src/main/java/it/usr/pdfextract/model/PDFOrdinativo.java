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
public class PDFOrdinativo {
    private Integer numeroConto;
    private Integer sezioneDiTesoreria;
    private Date dataOrdine;
    private Integer numeroOrdine;
    private Integer annoEmissione;
    private String statoTitolo;
    private BigDecimal importo;
    private String importoInLettere;
    private String modalitaEstinzione;
    private String provenienzaFondi;
    private String capitolo;
    private String ragioneria;
    private String previsione;
    private String appendice;
    private String articolo;
    private String codiceAmministrazioneAutonoma;
    private String sezioneProvenienzaFondi;
    private String contoCSProvenienzaFondi;
    private String contoTUProvenienzaFondi;
    private String codiceMinisteroFD;
    private String codcieQualificaFD;
    private String progressivoFD;
    private String tesoreriaFD;
    private String dettaglioProvenienzaFondi;
    private String CIG;
    private String CUP;
    private String CPV;
    private Date dataEsigibilita;
    private String codiceGestionale;
    private Estinzione estinzione;
    private String numeroFattura;
    private Date dataFattura;
    private BigDecimal importoTotaleDocumento;
    private int pagine;
    
    public Integer getNumeroConto() {
        return numeroConto;
    }

    public void setNumeroConto(Integer numeroConto) {
        this.numeroConto = numeroConto;
    }

    public Integer getSezioneDiTesoreria() {
        return sezioneDiTesoreria;
    }

    public void setSezioneDiTesoreria(Integer sezioneDiTesoreria) {
        this.sezioneDiTesoreria = sezioneDiTesoreria;
    }

    public Date getDataOrdine() {
        return dataOrdine;
    }

    public void setDataOrdine(Date dataOrdine) {
        this.dataOrdine = dataOrdine;
    }

    public Integer getNumeroOrdine() {
        return numeroOrdine;
    }

    public void setNumeroOrdine(Integer numeroOrdine) {
        this.numeroOrdine = numeroOrdine;
    }

    public Integer getAnnoEmissione() {
        return annoEmissione;
    }

    public void setAnnoEmissione(Integer annoEmissione) {
        this.annoEmissione = annoEmissione;
    }

    public String getStatoTitolo() {
        return statoTitolo;
    }

    public void setStatoTitolo(String statoTitolo) {
        this.statoTitolo = statoTitolo;
    }

    public BigDecimal getImporto() {
        return importo;
    }

    public void setImporto(BigDecimal importo) {
        this.importo = importo;
    }

    public String getImportoInLettere() {
        return importoInLettere;
    }

    public void setImportoInLettere(String importoInLettere) {
        this.importoInLettere = importoInLettere;
    }

    public String getModalitaEstinzione() {
        return modalitaEstinzione;
    }

    public void setModalitaEstinzione(String modalitaEstinzione) {
        this.modalitaEstinzione = modalitaEstinzione;
    }

    public String getProvenienzaFondi() {
        return provenienzaFondi;
    }

    public void setProvenienzaFondi(String provenienzaFondi) {
        this.provenienzaFondi = provenienzaFondi;
    }

    public String getCapitolo() {
        return capitolo;
    }

    public void setCapitolo(String capitolo) {
        this.capitolo = capitolo;
    }

    public String getRagioneria() {
        return ragioneria;
    }

    public void setRagioneria(String ragioneria) {
        this.ragioneria = ragioneria;
    }

    public String getPrevisione() {
        return previsione;
    }

    public void setPrevisione(String previsione) {
        this.previsione = previsione;
    }

    public String getAppendice() {
        return appendice;
    }

    public void setAppendice(String appendice) {
        this.appendice = appendice;
    }

    public String getArticolo() {
        return articolo;
    }

    public void setArticolo(String articolo) {
        this.articolo = articolo;
    }

    public String getCodiceAmministrazioneAutonoma() {
        return codiceAmministrazioneAutonoma;
    }

    public void setCodiceAmministrazioneAutonoma(String codiceAmministrazioneAutonoma) {
        this.codiceAmministrazioneAutonoma = codiceAmministrazioneAutonoma;
    }

    public String getSezioneProvenienzaFondi() {
        return sezioneProvenienzaFondi;
    }

    public void setSezioneProvenienzaFondi(String sezioneProvenienzaFondi) {
        this.sezioneProvenienzaFondi = sezioneProvenienzaFondi;
    }

    public String getContoCSProvenienzaFondi() {
        return contoCSProvenienzaFondi;
    }

    public void setContoCSProvenienzaFondi(String contoCSProvenienzaFondi) {
        this.contoCSProvenienzaFondi = contoCSProvenienzaFondi;
    }

    public String getContoTUProvenienzaFondi() {
        return contoTUProvenienzaFondi;
    }

    public void setContoTUProvenienzaFondi(String contoTUProvenienzaFondi) {
        this.contoTUProvenienzaFondi = contoTUProvenienzaFondi;
    }

    public String getCodiceMinisteroFD() {
        return codiceMinisteroFD;
    }

    public void setCodiceMinisteroFD(String codiceMinisteroFD) {
        this.codiceMinisteroFD = codiceMinisteroFD;
    }

    public String getCodcieQualificaFD() {
        return codcieQualificaFD;
    }

    public void setCodcieQualificaFD(String codcieQualificaFD) {
        this.codcieQualificaFD = codcieQualificaFD;
    }

    public String getProgressivoFD() {
        return progressivoFD;
    }

    public void setProgressivoFD(String progressivoFD) {
        this.progressivoFD = progressivoFD;
    }

    public String getTesoreriaFD() {
        return tesoreriaFD;
    }

    public void setTesoreriaFD(String tesoreriaFD) {
        this.tesoreriaFD = tesoreriaFD;
    }

    public String getDettaglioProvenienzaFondi() {
        return dettaglioProvenienzaFondi;
    }

    public void setDettaglioProvenienzaFondi(String dettaglioProvenienzaFondi) {
        this.dettaglioProvenienzaFondi = dettaglioProvenienzaFondi;
    }

    public String getCIG() {
        return CIG;
    }

    public void setCIG(String CIG) {
        this.CIG = CIG;
    }

    public String getCUP() {
        return CUP;
    }

    public void setCUP(String CUP) {
        this.CUP = CUP;
    }

    public String getCPV() {
        return CPV;
    }

    public void setCPV(String CPV) {
        this.CPV = CPV;
    }

    public Date getDataEsigibilita() {
        return dataEsigibilita;
    }

    public void setDataEsigibilita(Date dataEsigibilita) {
        this.dataEsigibilita = dataEsigibilita;
    }

    public String getCodiceGestionale() {
        return codiceGestionale;
    }

    public void setCodiceGestionale(String codiceGestionale) {
        this.codiceGestionale = codiceGestionale;
    }

    public Estinzione getEstinzione() {
        return estinzione;
    }

    public void setEstinzione(Estinzione estinzione) {
        this.estinzione = estinzione;
    }    

    public String getNumeroFattura() {
        return numeroFattura;
    }

    public void setNumeroFattura(String numeroFattura) {
        this.numeroFattura = numeroFattura;
    }

    public Date getDataFattura() {
        return dataFattura;
    }

    public void setDataFattura(Date dataFattura) {
        this.dataFattura = dataFattura;
    }

    public BigDecimal getImportoTotaleDocumento() {
        return importoTotaleDocumento;
    }

    public void setImportoTotaleDocumento(BigDecimal importoTotaleDocumento) {
        this.importoTotaleDocumento = importoTotaleDocumento;
    }

    public int getPagine() {
        return pagine;
    }

    public void setPagine(int pagine) {
        this.pagine = pagine;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PDFOrdinativo{");
        sb.append("numeroConto=").append(numeroConto);
        sb.append(", sezioneDiTesoreria=").append(sezioneDiTesoreria);
        sb.append(", dataOrdine=").append(dataOrdine);
        sb.append(", numeroOrdine=").append(numeroOrdine);
        sb.append(", annoEmissione=").append(annoEmissione);
        sb.append(", statoTitolo=").append(statoTitolo);
        sb.append(", importo=").append(importo);
        sb.append(", importoInLettere=").append(importoInLettere);
        sb.append(", modalitaEstinzione=").append(modalitaEstinzione);
        sb.append(", provenienzaFondi=").append(provenienzaFondi);
        sb.append(", capitolo=").append(capitolo);
        sb.append(", ragioneria=").append(ragioneria);
        sb.append(", previsione=").append(previsione);
        sb.append(", appendice=").append(appendice);
        sb.append(", articolo=").append(articolo);
        sb.append(", codiceAmministrazioneAutonoma=").append(codiceAmministrazioneAutonoma);
        sb.append(", sezioneProvenienzaFondi=").append(sezioneProvenienzaFondi);
        sb.append(", contoCSProvenienzaFondi=").append(contoCSProvenienzaFondi);
        sb.append(", contoTUProvenienzaFondi=").append(contoTUProvenienzaFondi);
        sb.append(", codiceMinisteroFD=").append(codiceMinisteroFD);
        sb.append(", codcieQualificaFD=").append(codcieQualificaFD);
        sb.append(", progressivoFD=").append(progressivoFD);
        sb.append(", tesoreriaFD=").append(tesoreriaFD);
        sb.append(", dettaglioProvenienzaFondi=").append(dettaglioProvenienzaFondi);
        sb.append(", CIG=").append(CIG);
        sb.append(", CUP=").append(CUP);
        sb.append(", CPV=").append(CPV);
        sb.append(", dataEsigibilita=").append(dataEsigibilita);
        sb.append(", codiceGestionale=").append(codiceGestionale);
        sb.append(", estinzione=").append(estinzione);
        sb.append(", numeroFattura=").append(numeroFattura);
        sb.append(", dataFattura=").append(dataFattura);
        sb.append(", importoTotaleDocumento=").append(importoTotaleDocumento);
        sb.append(", pagine=").append(pagine);
        sb.append('}');
        return sb.toString();
    }            
}
