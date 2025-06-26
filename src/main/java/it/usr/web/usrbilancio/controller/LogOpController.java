/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.LogOperazioniRecord;
import it.usr.web.usrbilancio.service.LogOperazioniService;
import java.util.List;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class LogOpController extends BaseController {
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    LogOperazioniService los;
    List<LogOperazioniRecord> log;    
    LogOperazioniRecord logSelezionato;
    
    public void init() {
        aggiornaLog();
    }

    public List<LogOperazioniRecord> getLog() {
        return log;
    }        

    public LogOperazioniRecord getLogSelezionato() {
        return logSelezionato;
    }

    public void setLogSelezionato(LogOperazioniRecord logSelezionato) {
        this.logSelezionato = logSelezionato;
    }    
    
    public void aggiornaLog() {
        log = los.getLogOperazioni();
        logSelezionato = null;
    }
    
    public String getServizio(String s) {
        if(isEmpty(s)) return s;
        
        String[] parts = s.split("\\.");
        return parts[0];
    }
    
    public String getFunzione(String s) {
        if(isEmpty(s)) return s;
        
        String[] parts = s.split("\\.");
        return parts.length>1 ? parts[1] : parts[0];
    }
}
