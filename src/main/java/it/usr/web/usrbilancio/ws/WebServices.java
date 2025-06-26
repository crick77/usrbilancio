/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.ws;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import it.usr.web.producer.AppLogger;
import it.usr.web.service.AuthService;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.model.AccessInfo;
import it.usr.web.usrbilancio.model.Download;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import it.usr.web.usrbilancio.producer.DocumentFolder;
import it.usr.web.usrbilancio.ws.security.JWTVerify;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jooq.DSLContext;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@Path("/v1")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class WebServices {
    public final static String BEARER = "Bearer";
    public final static  String SECRET = "trytodecryptthisbastard!!";
    public final static String ISSUER = "USR";
    public final static long NO_EXPIRE = -1;
    public final static long EXPIRE_24H = 60*60*24*1000;
    @Inject
    AuthService as;
    @Inject
    SessionStorage ss;
    @Inject
    @DSLBilancio
    DSLContext ctx;
    @DocumentFolder
    @Inject
    String documentFolder;
    @AppLogger
    @Inject
    Logger log;
    
    public String createToken(String id, long expiration) {
        JWTCreator.Builder token = JWT.create().withSubject(id).withIssuer(ISSUER);
        if (expiration>NO_EXPIRE) {            
            token = token.withExpiresAt(new Date(System.currentTimeMillis() + expiration));
        }
        return token.sign(Algorithm.HMAC512(SECRET.getBytes()));
    }

    /**
     * 
     * @param token
     * @return 
     */
    public DecodedJWT decodeToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                    .withIssuer(ISSUER)
                    .build(); //Reusable verifier instance
            return verifier.verify(token);
        } catch (JWTVerificationException jwte) {
            return null;
        }
        catch(IllegalArgumentException iae) {
            System.err.println("Decoding JWT error IllegalArgument: "+iae);
            return null;
        }
        catch(Exception e) {
            System.err.println("Decoding JWT error Exception: "+e);
            return null;
        }
    }
 
    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON) 
    public Response loginJson(AccessInfo ai) {            
        if(as.authenticate(ai.getUsername(), ai.getPassword())) {                        
            String token = createToken(ai.getUsername(), EXPIRE_24H);
            if(ss.add(token))
                return Response.ok(token).build();
            else
                return Response.serverError().entity("Impossibile utilizzare lo storage di sessione.").build();
            
        }
        
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    
    @POST
    @Path("logout")
    public Response logout(@HeaderParam("Authorization") String token) {
        if(token!=null) {
            token = token.replace(BEARER, "").trim();
            
            if(ss.invalidate(token)) {            
                return Response.noContent().build();
            }
        }
        
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    
    @GET
    @Path("ordinativo/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getOrdinativi(@PathParam("from") String from, @PathParam("to") String to) {
        try {
            LocalDate ldFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate ldTo = LocalDate.parse(to, DateTimeFormatter.ofPattern("yyyyMMdd"));
        
            String sOrd = ctx.selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).orderBy(Tables.ORDINATIVO.DATA_PAGAMENTO).fetch().formatJSON();        
            
            return Response.ok(sOrd).build(); 
        }
        catch(DateTimeParseException dtpe) {
            log.error("Formato data non corretto {}", dtpe);
            
            return Response.serverError().entity("Formato data non coreetto.").build();
        }
    }
    
    @GET
    @Path("quietanza/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getQuietanze(@PathParam("from") String from, @PathParam("to") String to) {
        try {
            LocalDate ldFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate ldTo = LocalDate.parse(to, DateTimeFormatter.ofPattern("yyyyMMdd"));
        
            String sQui = ctx.selectFrom(Tables.QUIETANZA).where(Tables.QUIETANZA.DATA_PAGAMENTO.between(ldFrom, ldTo)).orderBy(Tables.QUIETANZA.DATA_PAGAMENTO).fetch().formatJSON();        
            
            return Response.ok(sQui).build(); 
        }
        catch(DateTimeParseException dtpe) {
            log.error("Formato data non corretto {}", dtpe);
            
            return Response.serverError().entity("Formato data non coreetto.").build();
        }
    }
    
    @GET
    @Path("quietanza/download/{id: \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getDownloadQuietanza(@PathParam("id") int id) {
        try {                    
            QuietanzaRecord q = ctx.selectFrom(Tables.QUIETANZA).where(Tables.QUIETANZA.ID.eq(id)).fetchSingle();        
            
            if("__blank.pdf".equalsIgnoreCase(q.getNomefileLocale())) return Response.noContent().build();
            
            byte[] data = Files.readAllBytes(java.nio.file.Path.of(documentFolder+"/"+q.getNomefileLocale()));
            Download d = new Download(q.getNomefile(), data);
            
            return Response.ok(d).build(); 
        }
        catch(IOException ioe) {
            log.error("Impossibile accedere al file ID={}: {}", id, ioe);
            
            return Response.serverError().entity("Impossibile accedere al file.").build();
        }
    }
    
    @GET
    @Path("ordinativo/download/{id: \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    @JWTVerify
    public Response getDownloadOrdinativo(@PathParam("id") int id) {
        Integer idAllegato = null;
        
        try {                    
            List<AllegatoRecord> allegati = ctx.selectFrom(Tables.ALLEGATO).where(Tables.ALLEGATO.ID_ORDINATIVO.eq(id)).fetch();        
            
            if(allegati.isEmpty()) return Response.noContent().build();
            
            List<Download> downloads = new ArrayList<>();
            for(AllegatoRecord a : allegati) {
                idAllegato = a.getId();
                byte[] data = Files.readAllBytes(java.nio.file.Path.of(documentFolder+"/"+a.getNomefileLocale()));
                downloads.add(new Download(a.getNomefile(), data));
            }
                        
            return Response.ok(downloads).build(); 
        }
        catch(IOException ioe) {
            log.error("Impossibile accedere all'allegato ID={}: {}", idAllegato, ioe);
            
            return Response.serverError().entity("Impossibile accedere al file.").build();
        }
    }
}
