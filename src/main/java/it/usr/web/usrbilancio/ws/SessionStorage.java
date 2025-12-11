/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.ws;

import com.auth0.jwt.interfaces.DecodedJWT;
import it.usr.web.producer.AppLogger;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import java.util.Date;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */

@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
@Lock(LockType.READ)
public class SessionStorage {
    @Inject
    WebServices ws;
    private Map<String, DecodedJWT> session;
    @AppLogger
    @Inject
    Logger log;
    
    /**
     * 
     */
    @PostConstruct
    public void startup() {
        session = new HashMap<>();
        log.info("SessionStorage created.");
    }
    
    /**
     * 
     */
    @PreDestroy
    @Lock(LockType.WRITE)
    public void shutdown() {
        flush();
    }
    
    /**
     * 
     * @param token 
     * @return  
     */
    public boolean add(String token) {
        if(token!=null) {
            try {
                DecodedJWT jwt = ws.decodeToken(token);
                if(jwt!=null) {
                    //purge();
                    
                    session.put(token, jwt);
                    return true; 
                }
            }
            catch(Exception e) {
            }
        } 
        
        return false;
    }
    
    /**
     * 
     * @param token
     * @return 
     */
    @Lock(LockType.WRITE)
    public boolean isValid(String token) {
        if(token!=null) {
            return (session.get(token)!=null);
        }
        return false;
    }
    
    /**
     * 
     * @param token
     * @return 
     */
    public DecodedJWT get(String token) {
        if(token!=null) {
            return session.get(token);
        }
        return null;
    }
    
    /**
     * 
     * @param token 
     * @return  
     */
    @Lock(LockType.WRITE)
    public boolean invalidate(String token) {
        if(token!=null) {
            return (session.remove(token)!=null);
        }
        return false;
    }
    
    /**
     * 
     */
    @Lock(LockType.WRITE)
    public void flush() {
        session.clear();        
    }
    
    /**
     * 
     */
    @Lock(LockType.WRITE)
    @Schedule(hour = "2", persistent = false)
    public void purge() {
        int original = session.size();
        log.info("Avviamento pulizia programmata sessioni scadute. Sessioni attive: {}", original);
        Date term = new Date(System.currentTimeMillis()+WebServices.EXPIRE_24H);
        session.entrySet().removeIf(e -> {
            Date d = e.getValue().getExpiresAt();
            return d!=null && d.after(term);
        });        
        int newSize = session.size();
        log.info("Pulizia programmata sessioni scadute terminata. Sessioni attive: {}, Sessioni eliminate: {}.", newSize, (original-newSize));
    }
} 