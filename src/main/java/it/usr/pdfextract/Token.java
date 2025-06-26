/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.pdfextract;

/**
 *
 * @author riccardo.iovenitti
 */
public class Token {
    private String token;
    private int length;
    private String splitChar;
    private int expectedParts;

    public Token(String token) {
        this(token, null, 0);
    }

    public Token(String token, String splitChar, int expectedParts) {
        this.token = token;
        length = (token!=null) ? token.length() : 0;
        this.splitChar = splitChar;
        this.expectedParts = expectedParts;
    }

    public String getToken() {
        return token;
    }

    public int getLength() {
        return length;
    }

    public String getSplitChar() {
        return splitChar;
    }     

    public boolean hasSplitChar() {
        return splitChar!=null && !splitChar.equals("");
    }
    
    public int getExpectedParts() {
        return expectedParts;
    }
    
    public String getSanitizeed() {        
        return sanitize(this.token);
    }
    
    public static String sanitize(String token) {
       token = token.replace("à", "a").replace("ò", "o").replace("ì", "i").replace("è", "e").replace("é", "e").replace("ù", "u");
       return token.replaceAll("[^a-zA-Z0-9]", ""); 
    }
}
