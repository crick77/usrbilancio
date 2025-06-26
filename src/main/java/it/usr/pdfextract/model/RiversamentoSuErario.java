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
@ModalitaEstinzione("71")
public class RiversamentoSuErario implements Estinzione {
    private String indicatoreCompetenzaResidui;
    private Integer capitolo;
    private Integer articolo;
    private Integer capo;
    private String versante;
    private String CFVersante;
    private String codiceVersante;
    private Integer sezioneCompetenza;
    private String descrizioneCausale;
    private String firmatoDigitalmenteDa;
    private Date inData;

    public String getIndicatoreCompetenzaResidui() {
        return indicatoreCompetenzaResidui;
    }

    public void setIndicatoreCompetenzaResidui(String indicatoreCompetenzaResidui) {
        this.indicatoreCompetenzaResidui = indicatoreCompetenzaResidui;
    }

    public Integer getCapitolo() {
        return capitolo;
    }

    public void setCapitolo(Integer capitolo) {
        this.capitolo = capitolo;
    }

    public Integer getArticolo() {
        return articolo;
    }

    public void setArticolo(Integer articolo) {
        this.articolo = articolo;
    }

    public Integer getCapo() {
        return capo;
    }

    public void setCapo(Integer capo) {
        this.capo = capo;
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

    public Integer getSezioneCompetenza() {
        return sezioneCompetenza;
    }

    public void setSezioneCompetenza(Integer sezioneCompetenza) {
        this.sezioneCompetenza = sezioneCompetenza;
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
        return new Token[] { new Token("Indicatore Competenza / Residui:"),
                      new Token("Capitolo:"), 
                      new Token("Articolo:"), 
                      new Token("Capo:"), 
                      new Token("Versante:"), 
                      new Token("CF Versante:"), 
                      new Token("Codice Versante:"), 
                      new Token("Sezione competenza:"), 
                      new Token("Descrizione Causale:"), 
                      new Token("Firmato digitalmente da:"), 
                      new Token("in data:")
                    };
    }
        
    @Override
    public String toString() {
        return "RiversamentoSuErario{" + "indicatoreCompetenzaResidui=" + indicatoreCompetenzaResidui + ", capitolo=" + capitolo + ", articolo=" + articolo + ", capo=" + capo + ", versante=" + versante + ", CFVersante=" + CFVersante + ", codiceVersante=" + codiceVersante + ", sezioneCompetenza=" + sezioneCompetenza + ", descrizioneCausale=" + descrizioneCausale + ", firmatoDigitalmenteDa=" + firmatoDigitalmenteDa + ", inData=" + inData + '}';
    }        
}
