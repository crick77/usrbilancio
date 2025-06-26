/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.usr.web.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javax.naming.NamingException;
import org.slf4j.Logger;
import it.usr.web.producer.AppLogger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@Named
public class AuthService {
    public final static String SERVER_NAME = "aqadmc04"; //aqadmc05
    public final static String DOMAIN_NAME = "abruzzo.loc";    
    @AppLogger
    @Inject
    Logger logger;
    
    public boolean authenticate(String username, String password) {
        try {
            ActiveDirectory.getConnection(username, password, DOMAIN_NAME, SERVER_NAME).close();            
            logger.debug("Autenticazione utente [{}] riuscita!", username);
            return true;
        }
        catch(NamingException ne) {
            logger.debug("Autenticazione utente [{}] non riuscita a causa di: ", username, ne);
            return false;
        }
    }
}
