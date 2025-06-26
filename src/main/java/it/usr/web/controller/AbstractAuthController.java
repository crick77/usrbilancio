/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.usr.web.controller;

import it.usr.web.domain.ActiveUser;
import it.usr.web.domain.AppUser;
import it.usr.web.service.AuthService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import it.usr.web.producer.AppLogger;

/**
 *
 * @author riccardo.iovenitti
 */

public abstract class AbstractAuthController extends BaseController {
    @Inject
    private AuthService as;
    @Inject
    @AppLogger
    Logger logger;
    @Inject
    ActiveUser user;
    private String username;
    private String password;
    private String message;
    
    public String doLogin() {        
        if(as.authenticate(username, password)) {
            Object _attr = getUser(username);
            if(_attr!=null) {
                AppUser u = new AppUser(username, _attr);
                user.setCurrentUser(u);
                putIntoSession(u);
                logger.debug("L'utente [{}] ha effettuato l'accesso.", username);
                return redirect("/secure/index");
            }
        }
                
        logger.debug("L'utente [{}] non esiste o la password è errata o l'utente non abilitato.", username);
        message = "Credenziali di accesso errate o utente non abilitato.";
        return SAME_VIEW;        
    }
    
    public String doLogout() {
        invalidateSession();
        logger.debug("L'utente [{}] ha effettuato la disconnessione.", user.getCurrentUser().getUsername());
        return redirect("/index");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }        

    public String getMessage() {
        return message;
    }        

    protected abstract Object getUser(String username);
}
