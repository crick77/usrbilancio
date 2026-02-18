/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.CapitoloRecord;
import it.usr.web.usrbilancio.domain.tables.records.CompetenzaRecord;
import it.usr.web.usrbilancio.service.CapitoloService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.DuplicationException;
import it.usr.web.usrbilancio.service.IntegrityException;
import it.usr.web.usrbilancio.service.StaleRecordException;
import java.util.List;
import jakarta.ejb.EJBException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class CapitoliController extends BaseController {
    @Inject
    CapitoloService capServ;
    @Inject
    CompetenzaService compServ;
    List<CapitoloRecord> capitoli;
    List<CompetenzaRecord> competenze;
    CapitoloRecord capitolo;    
    CapitoloRecord capitoloSelezonato;
    CompetenzaRecord competenza;
    @Inject
    @AppLogger
    Logger logger;
    
    public void init() {
        capitoli = capServ.getCapitoli(true);
       
        competenze = null;
        capitolo = null;
        capitoloSelezonato = null;       
        competenza = null;
    }

    public List<CapitoloRecord> getCapitoli() {
        return capitoli;
    }        

    public List<CompetenzaRecord> getCompetenze() {
        return competenze;
    }
            
    public CapitoloRecord getCapitolo() {
        return capitolo;
    }

    public void setCapitolo(CapitoloRecord capitolo) {
        this.capitolo = capitolo;        
    }

    public CompetenzaRecord getCompetenza() {
        return competenza;
    }

    public void setCompetenza(CompetenzaRecord competenza) {
        this.competenza = competenza;
    }
        
    public CapitoloRecord getCapitoloSelezonato() {
        return capitoloSelezonato;
    }

    public void setCapitoloSelezonato(CapitoloRecord capitoloSelezonato) {
        this.capitoloSelezonato = capitoloSelezonato;

        aggiornaCompetenze();
    }
                           
    public void nuovo() {
        capitoloSelezonato = null;
        capitolo = new CapitoloRecord();
        capitolo.setNuovoanno((byte)0);
        capitolo.setVersione(0L);        
    }
    
    public void salva() {
        try {
            if(capitolo.getId()==null)
                capServ.inserisci(capitolo);
            else
                capServ.modifica(capitolo);
            init();
            PrimeFaces.current().executeScript("PF('capitoloDialog').hide();");
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato aggiornato da un altro utente. Aggiornare e riprovare."));
            }
            else if(ex.getCausedByException() instanceof DuplicationException) {
                addMessage(Message.warn("Esiste già un capitolo di spesa per il codice indicato."));                
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il salvataggio del capitolo {}. Errrore: {}", ex.getCausedByException().getClass(), capitolo, ex.getCausedByException());
            }
        }        
    }
    
    public void elimina() {
        try {
            capServ.elimina(capitolo);
            init();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof IntegrityException) {
                addMessage(Message.warn("Il capitolo non può essere eliminato per la presenta di dati collegati."));
            }
            else {            
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione del capitolo {}. Errrore: {}", ex.getCausedByException().getClass(), capitolo, ex.getCausedByException());            
            }
        }
    }  
    
    public void annulla() {
        init();
    }
    
    public void aggiornaCapitoli() {
        capitoli = capServ.getCapitoli();
        capitoloSelezonato = null;
        competenze = null;
    }
    
    public void aggiornaCompetenze() {
        if(capitoloSelezonato!=null) {
            competenze = compServ.getCompetenzeCapitolo(capitoloSelezonato.getId());
        }
        else {
            competenze = null;
        }
    }
    
    public void nuovaCompetenza() {
        competenza = new CompetenzaRecord();
        competenza.setIdCapitolo(capitoloSelezonato.getId());
        if(capitoloSelezonato.getStanziamento()!=null) competenza.setStanziamento(capitoloSelezonato.getStanziamento());
    }
    
    public void salvaCompetenza() {
         try {
            if(competenza.getId()==null)
                compServ.inserisci(competenza);
            else
                compServ.modifica(competenza);
            
            aggiornaCompetenze();
            PrimeFaces.current().executeScript("PF('competenzaDialog').hide();");
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato aggiornato da un altro utente. Aggiornare e riprovare."));
            }
            else if(ex.getCausedByException() instanceof DuplicationException) {
                addMessage(Message.warn("Esiste già una competenza per l'anno indicato."));                
            }
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il salvataggio della competenza {}. Errrore: {}", ex.getCausedByException().getClass(), competenza, ex.getCausedByException());
            }
        }  
    }
    
    public void eliminaCompetenza() {
        try {
            compServ.elimina(competenza);
            
            aggiornaCompetenze();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof IntegrityException) {
                addMessage(Message.warn("La competenza non può essere eliminata per la presenta di dati collegati."));
            }
            else {            
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione della competenza {}. Errrore: {}", ex.getCausedByException().getClass(), competenza, ex.getCausedByException());            
            }
        }
    }
    
    public void generaCompetenze() {
        try {
            int inseriti = compServ.generaCompetenze(getAnnoAttuale());
            CapitoloRecord sel = capitoloSelezonato;
            aggiornaCapitoli();
            capitoloSelezonato = sel;
            aggiornaCompetenze();
            
            addMessage(Message.info("Sono stati generate "+inseriti+" nuove competenze."));
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof IntegrityException) {
                addMessage(Message.error("Ci sono stati problemi nella generazione delle competenze. Probabile presenza di duplicati."));
                logger.debug("Errore di duplicazione durante la generazione delle competenze per l'anno {}. Errrore: {}", getAnnoAttuale(), ex.getCausedByException()); 
            }
            else {            
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante la generazione delle competenze per l'anno {}. Errrore: {}", ex.getCausedByException().getClass(), getAnnoAttuale(), ex.getCausedByException());            
            }
        }
    }
}
