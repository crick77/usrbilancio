/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.ws.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.ws.SessionStorage;
import it.usr.web.usrbilancio.ws.WebServices;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Provider
@JWTVerify
@Priority(Priorities.AUTHENTICATION)
public class JWTTokenFilter implements ContainerRequestFilter {     
    @Inject
    WebServices ws;
    @Inject
    SessionStorage ss;
    @AppLogger
    @Inject
    Logger log;
  
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String token = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        log.info("Controllo token [{}] per servizio [{}]", token, requestContext.getUriInfo().getPath());
        if(token!=null) {
            token = token.replace(WebServices.BEARER, "").trim();
            
            DecodedJWT jwt = ws.decodeToken(token);            
            if(jwt!=null) {
                log.info("Token decodificato: {}, subj: {}, scadenza: {}", jwt.getIssuer(), jwt.getSubject(), jwt.getExpiresAt());
                if(ss.isValid(token)) {
                    log.info("Il token risulta valido e in sessione.");
                    return;
                }
                else {
                    log.warn("ATTENZIONE: Il token non è più in sessione.");
                }
            }
            else {
                log.error("Impossibile decodificare il token [{}]", token);
            }
        }
        
        log.info("Autorizzazione non presente/valida. Restituzione 401.");
        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
}
