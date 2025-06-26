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
@ModalitaEstinzione("76")
public class RiversamentoSuCS implements Estinzione {
    private String ragioneSocialeDelBeneficiario;
    private String contoCS;
    private Integer sezioneCompetenza;
    private String versante;
    private String CFVersante;
    private String codiceVersante;
    private String descrizioneCausale;
    private String firmatoDigitalmenteDa;
    private Date inData;

    public String getRagioneSocialeDelBeneficiario() {
        return ragioneSocialeDelBeneficiario;
    }

    public void setRagioneSocialeDelBeneficiario(String ragioneSocialeDelBeneficiario) {
        this.ragioneSocialeDelBeneficiario = ragioneSocialeDelBeneficiario;
    }

    public String getContoCS() {
        return contoCS;
    }

    public void setContoCS(String contoCS) {
        this.contoCS = contoCS;
    }

    public Integer getSezioneCompetenza() {
        return sezioneCompetenza;
    }

    public void setSezioneCompetenza(Integer sezioneCompetenza) {
        this.sezioneCompetenza = sezioneCompetenza;
    }

    public String getVersante() {
        return versante;
    }

    public void setVersante(String versante) {
        this.versante = versante;
    }

    public String getCFVersante() {
        return CFVersante;
    }

    public void setCFVersante(String CFVersante) {
        this.CFVersante = CFVersante;
    }

    public String getCodiceVersante() {
        return codiceVersante;
    }

    public void setCodiceVersante(String codiceVersante) {
        this.codiceVersante = codiceVersante;
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
        return new Token[] { new Token("Ragione Sociale del Beneficiario :"),
                      new Token("Conto CS:"), 
                      new Token("Sezione competenza:"), 
                      new Token("Versante:"), 
                      new Token("CF Versante:"), 
                      new Token("Codice Versante:"), 
                      new Token("Descrizione Causale:"), 
                      new Token("Firmato digitalmente da:"), 
                      new Token("in data:")
                    };
    }
        
    @Override
    public String toString() {
        return "OneriImposte{" + "ragioneSocialeDelBeneficiario=" + ragioneSocialeDelBeneficiario + ", contoCS=" + contoCS + ", sezioneCompetenza=" + sezioneCompetenza + ", versante=" + versante + ", CFVersante=" + CFVersante + ", codiceVersante=" + codiceVersante + ", descrizioneCausale=" + descrizioneCausale + ", firmatoDigitalmenteDa=" + firmatoDigitalmenteDa + ", inData=" + inData + '}';
    }
}
