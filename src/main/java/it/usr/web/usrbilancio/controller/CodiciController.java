/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.DuplicationException;
import it.usr.web.usrbilancio.service.IntegrityException;
import it.usr.web.usrbilancio.service.StaleRecordException;
import jakarta.ejb.EJBException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class CodiciController extends BaseController {
    @Inject
    CodiceService cs;
    @Inject
    @AppLogger
    Logger logger;
    List<CodiceRecord> codici;
    CodiceRecord codice;
    String codiceComposto;
    String azione;
    
    public void init() {
        codici = cs.getCodici();
        codice = null;
        azione = null;
        codiceComposto = null;
    }

    public List<CodiceRecord> getCodici() {
        return codici;
    }

    public CodiceRecord getCodice() {
        return codice;
    }

    public void setCodice(CodiceRecord codice) {
        this.codice = codice;        
    }

    public String getAzione() {
        return azione;
    }

    public String getCodiceComposto() {
        return codiceComposto;
    }

    public void setCodiceComposto(String codiceComposto) {
        this.codiceComposto = codiceComposto;
    }                
    
    public void nuovo() {
        codice = new CodiceRecord();
        codiceComposto = null;
        azione = "Nuovo";
    }
    
    public void modifica() {
        codiceComposto = Formatter.formattaCodice(codice);
        azione = "Modifica";
    }
    
    public void salva() {
        String[] cc = codiceComposto.split("\\.");
        if(cc.length>6) {
            addMessage(Message.warn("Formato codice errato: sono permessi al massimo 6 blocchi totali."));
            return;
        }
        
        codice.setCodice(trimToSize(cc[0], 64));
        if(cc.length>1) codice.setC01(trimToSize(cc[1], 8)); else codice.setC01(null);
        if(cc.length>2) codice.setC02(trimToSize(cc[2], 8)); else codice.setC02(null);
        if(cc.length>3) codice.setC03(trimToSize(cc[3], 8)); else codice.setC03(null);
        if(cc.length>4) codice.setC04(trimToSize(cc[4], 8)); else codice.setC04(null);
        if(cc.length==6) codice.setC05(trimToSize(cc[5], 8)); else codice.setC05(null);
        
        if("PUB".equalsIgnoreCase(codice.getCodice()) && isEmpty(codice.getOrdinanza())) {
            addMessage(Message.warn("L'ordinanza è obbligatoria per i conti 'PUB'."));
            return;
        }
        
        try {
            if(codice.getId()!=null) {
                cs.modifica(codice);
            }
            else {
                cs.inserisci(codice);
            } 

            init();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof DuplicationException || ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn(ex.getCausedByException().getMessage()));
            }                        
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione del codice {}. Errrore: {}", ex.getCausedByException().getClass(), codice, ex.getCausedByException());
            }
        } 
    }
    
    public void annulla() {
        init();
    }
    
    public void elimina() {
        try {
            cs.elimina(codice);        
            
            init();
        }
        catch(EJBException ex) {
            if(ex.getCausedByException() instanceof IntegrityException) {
                addMessage(Message.warn("Il codice non può essere eliminato perchè collegato a uno o più movimenti."));
                init();
            }                        
            else {
                addMessage(Message.error("Errore imprevisto: "+ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione del codice {}. Errrore: {}", ex.getCausedByException().getClass(), codice, ex.getCausedByException());
            }
        } 
    }
    
    private String trimToSize(String s, int l) {
        if(s==null || s.length()<l) return s;
        return s.substring(1, l);
    }
    
    public boolean filtroConclusi(Object value, Object filter, Locale locale) {
        try {
            if("nullo".equalsIgnoreCase(String.valueOf(filter))) return true;
            Boolean v = Boolean.valueOf(String.valueOf(value));
            Boolean f = Boolean.valueOf(String.valueOf(filter));            
            return Objects.equals(v, f);            
        } catch (NumberFormatException nfe) {
            logger.error("Filtro per codici concluso non valido: {}, valore: {}", filter, value);
            return true;
        }
    }
    
    public void caricaUltimo() {
        if(isEmpty(codiceComposto)) {
            addMessage(Message.warn("Compilare il campo del codice e riprovare"));
            return;
        }
        
        String c = codiceComposto.toUpperCase().trim();
        if(!c.endsWith(".")) {
            addMessage(Message.warn("Il codice deve terminare con un '.' (punto). Correggere e riprovare."));
            return;
        }
        
        c = c.substring(0, c.length() - 1);
        String[] cc = c.split("\\.");
        
        CodiceRecord _codice = new CodiceRecord();
        _codice.setCodice(trimToSize(cc[0], 64)); 
        if(cc.length>1) _codice.setC01(trimToSize(cc[1], 8)); else _codice.setC01(null);
        if(cc.length>2) _codice.setC02(trimToSize(cc[2], 8)); else _codice.setC02(null);
        if(cc.length>3) _codice.setC03(trimToSize(cc[3], 8)); else _codice.setC03(null);
        if(cc.length>4) _codice.setC04(trimToSize(cc[4], 8)); else _codice.setC04(null);
        if(cc.length==6) _codice.setC05(trimToSize(cc[5], 8)); else _codice.setC05(null);
        
        CodiceRecord last = cs.cercaUltimo(_codice);
        if(last!=null) {
            codiceComposto = Formatter.formattaCodice(last);
        }
        else {
            addMessage(Message.warn("Non è stato trovato alcun conto in archivio con i codici indicati."));
        }
    }
}
  