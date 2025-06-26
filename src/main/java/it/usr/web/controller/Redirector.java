/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 *
 * @author riccardo.iovenitti
 */
public class Redirector {
    private final String path;
    private final List<String> params = new ArrayList<>();
    private boolean doRedirect;
    
    private Redirector(String path, List<String> params, boolean doRedirect) {
        this.path = path;        
        this.params.addAll(params);
        this.doRedirect = doRedirect;
    }
    
    public static Redirector toPath(String path) {
        if(path==null) throw new IllegalArgumentException("no path");
        
        List<String> mParams = new ArrayList<>();
        boolean doRedirect = (path.toLowerCase().contains("faces-redirect=true"));
        int idx = path.indexOf("?");        
        if(idx>=0) {
            String basePath = path.substring(0, idx);
            String params = path.substring(idx+1);
            String[] aParams = params.split("&");
            for(String p : aParams) {
                String[] pVal = p.split("=");
                if(!pVal[0].toUpperCase().contains("FACES-REDIRECT") && pVal.length==2) {
                    mParams.add(pVal[0]+"="+pVal[1]);
                }                
            }
            
            path = basePath;
        }
        
        return new Redirector(path, mParams, doRedirect);
    }
    
    public Redirector withParam(String param, Object value) {
        params.add(param+"="+String.valueOf(value));
        return this;
    }
    
    public Redirector withRedirect() {
        doRedirect = true;
        return this;
    }
    
    public String go() {        
        if(doRedirect) params.add("faces-redirect=true");
        StringJoiner sj = new StringJoiner("&", "", "");
        for(String p : params) sj.add(p);
        String pSj = sj.toString();
        return path+(pSj.length()>0 ? "?" : "")+pSj;
    }
}
