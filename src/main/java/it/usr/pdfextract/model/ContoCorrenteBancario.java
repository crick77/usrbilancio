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
@ModalitaEstinzione("56")
public class ContoCorrenteBancario implements Estinzione {
    private String ragioneSocialeDelBeneficiario;
    private String nomeECognomeDelBeneficiario;
    private String tipoCodiceIndividuale;
    private String codiceIndividuale;
    private String BIC;
    private String codiceIBAN;
    private String clienteOrdinante;
    private String causaleValutaria;
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

    public String getNomeECognomeDelBeneficiario() {
        return nomeECognomeDelBeneficiario;
    }

    public void setNomeECognomeDelBeneficiario(String nomeECognomeDelBeneficiario) {
        this.nomeECognomeDelBeneficiario = nomeECognomeDelBeneficiario;
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

    public String getBIC() {
        return BIC;
    }

    public void setBIC(String BIC) {
        this.BIC = BIC;
    }

    public String getCodiceIBAN() {
        return codiceIBAN;
    }

    public void setCodiceIBAN(String codiceIBAN) {
        this.codiceIBAN = codiceIBAN;
    }

    public String getClienteOrdinante() {
        return clienteOrdinante;
    }

    public void setClienteOrdinante(String clienteOrdinante) {
        this.clienteOrdinante = clienteOrdinante;
    }

    public String getCausaleValutaria() {
        return causaleValutaria;
    }

    public void setCausaleValutaria(String causaleValutaria) {
        this.causaleValutaria = causaleValutaria;
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
        return new Token[] { new Token("Ragione Sociale del Beneficiario:"),
                      new Token("Nome e Cognome del Beneficiario:"), 
                      new Token("Tipo Codice Individuale:"), 
                      new Token("BIC:"), 
                      new Token("Codice IBAN:"), 
                      new Token("Cliente ordinante:"), 
                      new Token("Causale valutaria:"), 
                      new Token("Paese di residenza:"), 
                      new Token("Descrizione Causale:"), 
                      new Token("Firmato digitalmente da:"), 
                      new Token("in data:")
                    };
    }
        
    @Override
    public String toString() {
        return "ContoCorrenteBancario{" + "ragioneSocialeDelBeneficiario=" + ragioneSocialeDelBeneficiario + ", nomeECognomeDelBeneficiario=" + nomeECognomeDelBeneficiario + ", tipoCodiceIndividuale=" + tipoCodiceIndividuale + ", codiceIndividuale=" + codiceIndividuale + ", BIC=" + BIC + ", codiceIBAN=" + codiceIBAN + ", clienteOrdinante=" + clienteOrdinante + ", causaleValutaria=" + causaleValutaria + ", paeseDiResidenza=" + paeseDiResidenza + ", descrizioneCausale=" + descrizioneCausale + ", firmatoDigitalmenteDa=" + firmatoDigitalmenteDa + ", inData=" + inData + '}';
    }        
}
