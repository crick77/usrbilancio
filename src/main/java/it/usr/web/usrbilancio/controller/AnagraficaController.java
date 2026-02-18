/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.AnagraficaRecord;
import it.usr.web.usrbilancio.domain.tables.records.ComuneRecord;
import it.usr.web.usrbilancio.service.AnagraficaService;
import it.usr.web.usrbilancio.service.DuplicationException;
import it.usr.web.usrbilancio.service.StaleRecordException;
import jakarta.ejb.EJBException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.stream.Collectors;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class AnagraficaController extends BaseController {

    @Inject
    AnagraficaService as;
    @Inject
    @AppLogger
    Logger logger;
    List<AnagraficaRecord> anagrafica;
    AnagraficaRecord anagraficaSelezionata;
    List<ComuneRecord> comuni;
    String azione;

    public void init() {
        comuni = as.getComuni();
        
        aggiorna();
    }

    public List<AnagraficaRecord> getAnagrafica() {
        return anagrafica; 
    }

    public AnagraficaRecord getAnagraficaSelezionata() {
        return anagraficaSelezionata;
    }
   
    public String getAzione() {
        return azione;
    }

    public List<ComuneRecord> getComuni() {
        return comuni;
    }
        
    public void aggiorna() {
        anagrafica = as.getAnagrafica();
        anagraficaSelezionata = null;
        azione = null;
    }

    public void nuovo() {
        anagraficaSelezionata = new AnagraficaRecord();
        azione = "Nuova";
    }

    public void modifica(AnagraficaRecord ar) {
        anagraficaSelezionata = ar;
        azione = "Modifica";
    }

    public void elimina(AnagraficaRecord ar) {
        try {
            if(ar!=null) {
                as.elimina(ar);
                addMessage(Message.info("Anagrafica eliminata con sucesso."));

                aggiorna();
            }
        } 
        catch (EJBException ex) {
            if (ex.getCausedByException() instanceof DuplicationException || ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn(ex.getCausedByException().getMessage()));
            } else {
                addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione dell'anagrafica {}. Errore: {}", ex.getCausedByException().getClass(), ar, ex.getCausedByException());
            }
        }
    }
    
    public void salva() {
        try {
            if(anagraficaSelezionata.getId()!=null) {
                as.modifica(anagraficaSelezionata);
                addMessage(Message.info("Anagrafica modificata con sucesso."));
            }
            else {
                as.inserisci(anagraficaSelezionata);
                addMessage(Message.info("Anagrafica inserita con sucesso."));
            }

            aggiorna();

            PrimeFaces.current().executeScript("PF('anagraficaDialogWV').hide();");
        } 
        catch (EJBException ex) {
            if (ex.getCausedByException() instanceof DuplicationException || ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn(ex.getCausedByException().getMessage()));
            } else {
                addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'inserimento/modifica dell'anagrafica {}. Errore: {}", ex.getCausedByException().getClass(), anagraficaSelezionata, ex.getCausedByException());
            }
        }
    } 

    public void annulla() {
        aggiorna();
 
        PrimeFaces.current().executeScript("PF('anagraficaDialogWV').hide();");
    }        
    
    public List<ComuneRecord> cercaComune(String query) {
        if(isEmpty(query)) return null;
        
        final String fQuery = query.trim().toUpperCase();
        
        return comuni.stream().filter(c -> c.getComune().toUpperCase().contains(fQuery)).collect(Collectors.toList());
    }   
     
    public void onComuneSelect(SelectEvent<String> event) {
        ComuneRecord cr = as.getComune(event.getObject());
        if(cr!=null) { 
            anagraficaSelezionata.setCitta(cr.getComune());
            anagraficaSelezionata.setProvincia(cr.getProv());
            anagraficaSelezionata.setCodiceCatastale(cr.getCodCatastale());
        }
    }
    
    public String formattaComune(Object cr) {
        if(cr==null) return null;        
        if(cr instanceof String string) return string;
        
        ComuneRecord _cr = (ComuneRecord)cr;
        StringBuilder sb = new StringBuilder();
        return sb.append(_cr.getComune()).append(" (").append(_cr.getProv()).append(")").toString();
    }  
} 
  