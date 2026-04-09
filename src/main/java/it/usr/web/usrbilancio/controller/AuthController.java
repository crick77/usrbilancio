/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.AbstractAuthController;
import it.usr.web.usrbilancio.domain.tables.records.ContabilitaRecord;
import it.usr.web.usrbilancio.domain.tables.records.UtenteRecord;
import it.usr.web.usrbilancio.service.CapitoloService;
import it.usr.web.usrbilancio.service.UtenteService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@RequestScoped
public class AuthController extends AbstractAuthController {
    @Inject
    UtenteService us;
    @Inject
    CapitoloService cs;
    List<ContabilitaRecord> contabilita;
    ContabilitaRecord contabilitaSelezionata;

    @PostConstruct
    public void init() {
        contabilita = cs.getContabilita();
        contabilitaSelezionata = null;
    }
    
    @Override
    protected Object getUser(String username) {
        UtenteRecord u = us.getUtente(username);
        return (u!=null && u.getAbilitato()==1) ?  u : null;
    }      

    @Override
    public String doLogin() {       
        String dest = super.doLogin();
        
        if((dest == null ? SAME_VIEW != null : !dest.equals(SAME_VIEW)) && contabilitaSelezionata==null) {
            setMessage("Selezionare una contabilità speciale da utilizzare durante la sessione.");
            return SAME_VIEW;
        }
        
        user.getAttributes().put("contabilita", contabilitaSelezionata);
        
        return dest;
    }

    public List<ContabilitaRecord> getContabilita() {
        return contabilita;
    }        

    public ContabilitaRecord getContabilitaSelezionata() {
        return contabilitaSelezionata;
    }

    public void setContabilitaSelezionata(ContabilitaRecord contabilitaSelezionata) {
        this.contabilitaSelezionata = contabilitaSelezionata;
    }        
}
