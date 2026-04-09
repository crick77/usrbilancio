/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.domain.ActiveUser;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.ContabilitaRecord;
import it.usr.web.usrbilancio.domain.tables.records.RettificaIvaRecord;
import it.usr.web.usrbilancio.service.DuplicationException;
import it.usr.web.usrbilancio.service.RettificaIvaService;
import it.usr.web.usrbilancio.service.StaleRecordException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.util.List;
import java.util.OptionalInt;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class RettificheIvaController extends BaseController {
    @Inject
    RettificaIvaService ris;
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    ActiveUser activeUser;
    ContabilitaRecord contabilita;
    List<RettificaIvaRecord> rettifiche;
    Integer anno;    
    
    public void init() {
        contabilita = (ContabilitaRecord)activeUser.getAttributes().get("contabilita");
        rettifiche = ris.getRettificheIVA(contabilita);
        anno = null;
    }

    public boolean isEditing() {
        return anno!=null;
    }
    
    public List<RettificaIvaRecord> getRettifiche() {
        return rettifiche;
    }
    
    public void nuovo() {
        OptionalInt max = rettifiche.stream().map(r -> r.getAnno()).mapToInt(Integer::intValue).max();
        int nuovo = max.isPresent() ? max.getAsInt()+1 : getAnnoAttuale();
        RettificaIvaRecord ri = new RettificaIvaRecord();
        ri.setIdContabilita(contabilita.getId());
        ri.setAnno(nuovo);
        ri.setIvaPagata(BigDecimal.ZERO);
        ri.setIvaAnagrafica(BigDecimal.ZERO);
        
        try {
            ris.inserisci(ri);
        }
        catch(DuplicationException e) {
            addMessage(Message.warn("Esiste già un record per l'anno indicato."));
        }
        catch(Exception e) {
            addMessage(Message.error("Errore inatteso: "+e.getMessage()));
        }
        
        init();
    }
    
    public void elimina(RettificaIvaRecord ri) {
        try {
            ris.elimina(ri);
            addMessage(Message.info("Rettifica eliminata."));           
        }
        catch(StaleRecordException e) {
            addMessage(Message.warn("Il record è stato già eliminato da un altro utente."));
        }
        catch(Exception e) {
            addMessage(Message.error("Errore inatteso: "+e.getMessage()));
        }
        
        init();
    }
    
    public void onRowEdit(RowEditEvent<RettificaIvaRecord> event) {
        try {
            ris.modifica(anno, event.getObject());
            addMessage(Message.info("Rettifica aggiornata."));           
        }
        catch(DuplicationException e) {
            addMessage(Message.warn("Esiste già un record per l'anno indicato."));
        }
        catch(Exception e) {
            addMessage(Message.error("Errore inatteso: "+e.getMessage()));
        }
        
        init();
        PrimeFaces.current().executeScript("refresh()");
    }
    
    public void onRowEditInit(RowEditEvent<RettificaIvaRecord> event) {
        anno = event.getObject().getAnno();
    }
    
    public void onRowCancel(RowEditEvent<RettificaIvaRecord> event) {
        anno = null;
    }
}
