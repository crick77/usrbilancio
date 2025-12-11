/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.model;

import java.io.Serializable;

/**
 *
 * @author riccardo.iovenitti
 */
public class DocumentoAllegato implements Serializable {
    private int id;
    private String tipo;
    private String nomeFile;
    private String gruppo;
    private String descrizione;

    public DocumentoAllegato() {
    }

    public DocumentoAllegato(int id, String tipo, String nomeFile, String gruppo, String descrizione) {
        this.id = id;
        this.tipo = tipo;
        this.nomeFile = nomeFile;
        this.gruppo = gruppo;
        this.descrizione = descrizione;
    }
        
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNomeFile() {
        return nomeFile;
    }

    public void setNomeFile(String nomeFile) {
        this.nomeFile = nomeFile;
    }

    public String getGruppo() {
        return gruppo;
    }

    public void setGruppo(String gruppo) {
        this.gruppo = gruppo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }
    
    
}
