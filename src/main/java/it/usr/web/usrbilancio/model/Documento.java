/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.model;

/**
 *
 * @author riccardo.iovenitti
 */
public class Documento {
    private String fileName;
    private String gruppo;
    private byte[] content;
    private String contentType;

    public Documento() {
    }
    
    public Documento(String fileName, String gruppo, byte[] content, String contentType) {
        this.fileName = fileName;
        this.gruppo = gruppo;
        this.content = content;
        this.contentType = contentType;
    }
        
    public String getGruppo() {
        return gruppo;
    }

    public void setGruppo(String gruppo) {
        this.gruppo = gruppo;
    }
             
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }      

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\"fileName\": \"").append(fileName).append("\",\n");
        sb.append("\"content\": ").append(content!=null ? content.length : -1).append(",\n");
        sb.append("\"contentType\": \"").append(contentType).append("\"\n");
        sb.append("\"gruppo\": \"").append(gruppo).append("\"\n");
        sb.append('}');
        return sb.toString();
    }

    
}
