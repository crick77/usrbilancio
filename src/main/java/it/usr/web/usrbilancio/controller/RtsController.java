/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.DuplicationException;
import it.usr.web.usrbilancio.service.IntegrityException;
import it.usr.web.usrbilancio.service.StaleRecordException;
import jakarta.ejb.EJBException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class RtsController extends BaseController {
    @Inject
    CodiceService cs;
    @Inject
    @AppLogger
    Logger logger;
    List<TipoRtsRecord> codiciRts;
    TipoRtsRecord codiceRts;
    String azione;
    
    public void init() {
        aggiorna();
    }

    public List<TipoRtsRecord> getCodiciRts() {
        return codiciRts;
    }

    public TipoRtsRecord getCodiceRts() {
        return codiceRts;
    }

    public void setCodiceRts(TipoRtsRecord codiceRts) {
        this.codiceRts = codiceRts;
    }

    public String getAzione() {
        return azione;
    }
              
    public void aggiorna() {
        codiciRts = cs.getTipiRts(CodiceService.GruppoRts.RTS_TUTTI);
        codiceRts = null;
        azione = null;
    }
    
    public void nuovo() {
        codiceRts = new TipoRtsRecord();
        azione = "Nuovo";
    }
    
    public void salva() {
        try {
            if(codiceRts.getId()!=null) {
                cs.modifica(codiceRts);
            }
            else {
                cs.inserisci(codiceRts);
            }

            aggiorna();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof DuplicationException || ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn(ex.getCausedByException().getMessage()));
            }                        
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione del codice RTS {}. Errrore: {}", ex.getCausedByException().getClass(), codiceRts, ex.getCausedByException());
            }
        } 
    }
    
    public void elimina() {
        try {
            cs.elimina(codiceRts);        
            
            aggiorna();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof IntegrityException) {
                addMessage(Message.warn("Il codice RTS non può essere eliminato perchè collegato a uno o più movimenti."));
                aggiorna();
            }                        
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione del codice RTS {}. Errrore: {}", ex.getCausedByException().getClass(), codiceRts, ex.getCausedByException());
            }
        } 
    }
    
    public void modifica() {
        azione = "Modifica";
    }
    
    public void annulla() {
        aggiorna();
    }
}
