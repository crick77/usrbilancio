/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.pdfextract;

/**
 *
 * @author riccardo.iovenitti
 */
public class QuietanzaToken {
    private final String token;
    private final String nextToken;
    private final int tokenLength;

    public QuietanzaToken(String token) {
        this.token = token;
        this.nextToken = null;
        this.tokenLength = -1;
    }

    public QuietanzaToken(String token, int tokenLength) {
        this.token = token;
        this.nextToken = null;
        this.tokenLength = tokenLength;
    }
    
    public QuietanzaToken(String token, String nextToken) {
        this.token = token;
        this.nextToken = nextToken;
        this.tokenLength = -1;
    }
    
    public QuietanzaToken(String token, String nextToken, int tokenLength) {
        this.token = token;
        this.nextToken = nextToken;
        this.tokenLength = tokenLength;
    }
    
    public String getToken() {
        return token;
    }

    public String getNextToken() {
        return nextToken;
    }        

    public int getTokenLength() {
        return tokenLength;
    }        
}
