/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.pdfextract.model;

import it.usr.pdfextract.ModalitaEstinzione;
import it.usr.pdfextract.Token;
import java.util.Date;

/**
 *
 * @author riccardo.iovenitti
 */
@ModalitaEstinzione("81")
public class RegolazionePagamentiInContoSospeso implements Estinzione {
    private String ragioneSocialeDelBeneficiario;
    private String nomeDelBeneficiario;
    private String cognomeDelBeneficiario;
    private Date dataDiNascita;
    private String luogoDiNascita;
    private String tipoCodiceIndividuale;
    private String codiceIndividuale;
    private String tipoSottoconto;
    private String identificativoPartita;
    private String ammOrdinante;
    private String sezioneCompetente;
    private String paeseDiResidenza;
    private String descrizioneCausale;
    private String firmatoDigitalmenteDa;
    private Date inData;

    public String getRagioneSocialeDelBeneficiario() {
        return ragioneSocialeDelBeneficiario;
    }

    public void setRagioneSocialeDelBeneficiario(String ragioneSocialeDelBeneficiario) {
        this.ragioneSocialeDelBeneficiario = ragioneSocialeDelBeneficiario;
    }

    public String getNomeDelBeneficiario() {
        return nomeDelBeneficiario;
    }

    public void setNomeDelBeneficiario(String nomeDelBeneficiario) {
        this.nomeDelBeneficiario = nomeDelBeneficiario;
    }

    public String getCognomeDelBeneficiario() {
        return cognomeDelBeneficiario;
    }

    public void setCognomeDelBeneficiario(String cognomeDelBeneficiario) {
        this.cognomeDelBeneficiario = cognomeDelBeneficiario;
    }

    public Date getDataDiNascita() {
        return dataDiNascita;
    }

    public void setDataDiNascita(Date dataDiNascita) {
        this.dataDiNascita = dataDiNascita;
    }

    public String getLuogoDiNascita() {
        return luogoDiNascita;
    }

    public void setLuogoDiNascita(String luogoDiNascita) {
        this.luogoDiNascita = luogoDiNascita;
    }

    public String getTipoCodiceIndividuale() {
        return tipoCodiceIndividuale;
    }

    public void setTipoCodiceIndividuale(String tipoCodiceIndividuale) {
        this.tipoCodiceIndividuale = tipoCodiceIndividuale;
    }

    public String getCodiceIndividuale() {
        return codiceIndividuale;
    }

    public void setCodiceIndividuale(String codiceIndividuale) {
        this.codiceIndividuale = codiceIndividuale;
    }

    public String getTipoSottoconto() {
        return tipoSottoconto;
    }

    public void setTipoSottoconto(String tipoSottoconto) {
        this.tipoSottoconto = tipoSottoconto;
    }

    public String getIdentificativoPartita() {
        return identificativoPartita;
    }

    public void setIdentificativoPartita(String identificativoPartita) {
        this.identificativoPartita = identificativoPartita;
    }

    public String getAmmOrdinante() {
        return ammOrdinante;
    }

    public void setAmmOrdinante(String ammOrdinante) {
        this.ammOrdinante = ammOrdinante;
    }

    public String getSezioneCompetente() {
        return sezioneCompetente;
    }

    public void setSezioneCompetente(String sezioneCompetente) {
        this.sezioneCompetente = sezioneCompetente;
    }

    public String getPaeseDiResidenza() {
        return paeseDiResidenza;
    }

    public void setPaeseDiResidenza(String paeseDiResidenza) {
        this.paeseDiResidenza = paeseDiResidenza;
    }

    public String getDescrizioneCausale() {
        return descrizioneCausale;
    }

    public void setDescrizioneCausale(String descrizioneCausale) {
        this.descrizioneCausale = descrizioneCausale;
    }

    public String getFirmatoDigitalmenteDa() {
        return firmatoDigitalmenteDa;
    }

    public void setFirmatoDigitalmenteDa(String firmatoDigitalmenteDa) {
        this.firmatoDigitalmenteDa = firmatoDigitalmenteDa;
    }

    public Date getInData() {
        return inData;
    }

    public void setInData(Date inData) {
        this.inData = inData;
    }
    
    @Override
    public Token[] getTokens() {
        return new Token[] {  new Token("Ragione Sociale del Beneficiario:"),
                      new Token("Nome del Beneficiario:"), 
                      new Token("Cognome del Beneficiario:"), 
                      new Token("Data di Nascita:"), 
                      new Token("Luogo di Nascita:"), 
                      new Token("Tipo Codice Individuale:"), 
                      new Token("Codice Individuale:"), 
                      new Token("Tipo sottoconto:"), 
                      new Token("Identificativo partita:"), 
                      new Token("Amm. Ordinante:"), 
                      new Token("Sezione competenza:"), 
                      new Token("Descrizione Causale:"), 
                      new Token("Firmato digitalmente da:"), 
                      new Token("in data:")
                    };
    }

    @Override
    public String toString() {
        return "RegolazionePagamentiInContoSospeso{" + "ragioneSocialeDelBeneficiario=" + ragioneSocialeDelBeneficiario + ", nomeDelBeneficiario=" + nomeDelBeneficiario + ", cognomeDelBeneficiario=" + cognomeDelBeneficiario + ", dataDiNascita=" + dataDiNascita + ", luogoDiNascita=" + luogoDiNascita + ", tipoCodiceIndividuale=" + tipoCodiceIndividuale + ", codiceIndividuale=" + codiceIndividuale + ", tipoSottoconto=" + tipoSottoconto + ", identificativoPartita=" + identificativoPartita + ", ammOrdinante=" + ammOrdinante + ", sezioneCompetente=" + sezioneCompetente + ", paeseDiResidenza=" + paeseDiResidenza + ", descrizioneCausale=" + descrizioneCausale + ", firmatoDigitalmenteDa=" + firmatoDigitalmenteDa + ", inData=" + inData + '}';
    }        
}
