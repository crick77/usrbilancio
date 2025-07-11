/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller.pubblica;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.model.Intervento;
import it.usr.web.usrbilancio.service.CodiceService;
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
public class InterventiPubblicaController extends BaseController {
    @Inject
    CodiceService cs;
    @Inject
    @AppLogger
    Logger logger;
    List<Intervento> interventi;
    Intervento intervento;
    
    public void init() {
        interventi = cs.getInterventiPubblica();
        intervento = null;
    }

    public List<Intervento> getInterventi() {
        return interventi;
    }

    public Intervento getIntervento() {
        return intervento;
    }

    public void setIntervento(Intervento intervento) {
        this.intervento = intervento;
    }    

    public void errorTest() {
        throw new RuntimeException("Test errore da "+getClass().getName());
    }
} 
