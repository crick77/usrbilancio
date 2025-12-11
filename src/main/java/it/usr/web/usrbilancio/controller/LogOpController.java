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
    List<LogOperazioniRecord> logFiltrati;    
    LogOperazioniRecord logSelezionato;
    String servizioFiltro;
    String operatoreFiltro;
    List<String> servizi;
    List<String> operatori;
        
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

    public String getServizioFiltro() {
        return servizioFiltro;
    }

    public void setServizioFiltro(String servizioFiltro) {
        this.servizioFiltro = servizioFiltro;
    }
    
    public List<String> getServizi() {
        return servizi;
    }

    public List<LogOperazioniRecord> getLogFiltrati() {
        return logFiltrati;
    }

    public void setLogFiltrati(List<LogOperazioniRecord> logFiltrati) {
        this.logFiltrati = logFiltrati; 
    } 

    public List<String> getOperatori() {
        return operatori;
    }

    public String getOperatoreFiltro() {
        return operatoreFiltro;
    }

    public void setOperatoreFiltro(String operatoreFiltro) {
        this.operatoreFiltro = operatoreFiltro;
    }
                  
    public void aggiornaLog() {
        log = los.getLogOperazioni();   
        servizi = los.getServizi();
        operatori = los.getOperatori();
        logSelezionato = null;
        servizioFiltro = null; 
        logFiltrati = null;   
        operatoreFiltro = null;        
    }       
}
