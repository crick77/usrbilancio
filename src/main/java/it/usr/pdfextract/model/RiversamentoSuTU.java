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
@ModalitaEstinzione("77")
public class RiversamentoSuTU implements Estinzione {
    private String ragioneSocialeDelBeneficiario;
    private String contoTU;
    private Integer sezioneCompetenza;
    private String indicatoreFruttiferoInfruttifero;
    private String clienteOrdinante;
    private String descrizioneCausale;
    private String firmatoDigitalmenteDa;
    private Date inData;

    public String getRagioneSocialeDelBeneficiario() {
        return ragioneSocialeDelBeneficiario;
    }

    public void setRagioneSocialeDelBeneficiario(String ragioneSocialeDelBeneficiario) {
        this.ragioneSocialeDelBeneficiario = ragioneSocialeDelBeneficiario;
    }

    public String getContoTU() {
        return contoTU;
    }

    public void setContoTU(String contoTU) {
        this.contoTU = contoTU;
    }

    public Integer getSezioneCompetenza() {
        return sezioneCompetenza;
    }

    public void setSezioneCompetenza(Integer sezioneCompetenza) {
        this.sezioneCompetenza = sezioneCompetenza;
    }

    public String getIndicatoreFruttiferoInfruttifero() {
        return indicatoreFruttiferoInfruttifero;
    }

    public void setIndicatoreFruttiferoInfruttifero(String indicatoreFruttiferoInfruttifero) {
        this.indicatoreFruttiferoInfruttifero = indicatoreFruttiferoInfruttifero;
    }

    public String getClienteOrdinante() {
        return clienteOrdinante;
    }

    public void setClienteOrdinante(String clienteOrdinante) {
        this.clienteOrdinante = clienteOrdinante;
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
                      new Token("Conto TU:"), 
                      new Token("Sezione competenza:"), 
                      new Token("Indicatore Fruttifero/Infruttifero:"), 
                      new Token("Cliente Ordinante:"), 
                      new Token("Descrizione Causale:"), 
                      new Token("Firmato digitalmente da:"), 
                      new Token("in data:")
                    };
    }
    
    @Override
    public String toString() {
        return "RiversamentoSuTU{" + "ragioneSocialeDelBeneficiario=" + ragioneSocialeDelBeneficiario + ", contoTU=" + contoTU + ", sezioneCompetenza=" + sezioneCompetenza + ", indicatoreFruttiferoInfruttifero=" + indicatoreFruttiferoInfruttifero + ", clienteOrdinante=" + clienteOrdinante + ", descrizioneCausale=" + descrizioneCausale + ", firmatoDigitalmenteDa=" + firmatoDigitalmenteDa + ", inData=" + inData + '}';
    }        
}
