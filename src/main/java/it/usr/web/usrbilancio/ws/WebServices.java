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
import static it.usr.web.usrbilancio.domain.Tables.*;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoRecord;
import it.usr.web.usrbilancio.domain.tables.records.ModelliRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.model.AccessInfo;
import it.usr.web.usrbilancio.model.Download;
import it.usr.web.usrbilancio.model.OperaPubblica;
import it.usr.web.usrbilancio.model.RiepilogoRGS;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import it.usr.web.usrbilancio.producer.DocumentFolder;
import it.usr.web.usrbilancio.service.CompetenzaService;
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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
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
    CompetenzaService cs;
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
    @Path("tipirts")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getTipiRts() {
        String sTipiRts = ctx.selectFrom(TIPO_RTS).fetch().formatJSON();        
        return Response.ok(sTipiRts).build();         
    }
    
    @GET
    @Path("ordinativo/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getOrdinativi(@PathParam("from") String from, @PathParam("to") String to) {
        try {
            LocalDate ldFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate ldTo = LocalDate.parse(to, DateTimeFormatter.ofPattern("yyyyMMdd"));
        
            int id99 = ctx.select(TIPO_RTS.ID).from(TIPO_RTS).where(TIPO_RTS.CODICE.eq("99")).fetchSingleInto(Integer.class);
            String sOrd = ctx.selectFrom(ORDINATIVO)
                            .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%").and(ORDINATIVO.ID_TIPO_RTS.ne(id99))))
                            .orderBy(ORDINATIVO.DATA_PAGAMENTO, ORDINATIVO.NUMERO_PAGAMENTO)
                            .fetch()
                            .formatJSON();        
            
            return Response.ok(sOrd).build(); 
        }
        catch(DateTimeParseException dtpe) {
            log.error("Formato data non corretto {}", dtpe);
            
            return Response.serverError().entity("Formato data non corretto.").build();
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
        
            int id99 = ctx.select(TIPO_RTS.ID).from(TIPO_RTS).where(TIPO_RTS.CODICE.eq("99")).fetchSingleInto(Integer.class);
            String sQui = ctx.selectFrom(QUIETANZA)
                            .where(QUIETANZA.DATA_PAGAMENTO.between(ldFrom, ldTo).and(DSL.upper(QUIETANZA.NUMERO_PAGAMENTO).notLike("NO RAG%").and(QUIETANZA.ID_TIPO_RTS.ne(id99))))
                            .orderBy(QUIETANZA.DATA_PAGAMENTO, QUIETANZA.NUMERO_PAGAMENTO)
                            .fetch()
                            .formatJSON();        
            
            return Response.ok(sQui).build(); 
        }
        catch(DateTimeParseException dtpe) {
            log.error("Formato data non corretto {}", dtpe);
            
            return Response.serverError().entity("Formato data non corretto.").build();
        }
    }
    
    @GET
    @Path("quietanza/download/{id: \\d+}")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getDownloadQuietanza(@PathParam("id") int id) {
        try {                    
            QuietanzaRecord q = ctx.selectFrom(QUIETANZA).where(QUIETANZA.ID.eq(id)).fetchSingle();        
            
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
    @Path("ordinativo/{from}/{to}/countfiles")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getConteggioFileOrdinativi(@PathParam("from") String from, @PathParam("to") String to) {
        try {             
            LocalDate ldFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate ldTo = LocalDate.parse(to, DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            int id99 = ctx.select(TIPO_RTS.ID).from(TIPO_RTS).where(TIPO_RTS.CODICE.eq("99")).fetchSingleInto(Integer.class);
            int count = ctx.select(DSL.count(ALLEGATO.ID))
                    .from(ALLEGATO.join(ORDINATIVO).on(ALLEGATO.ID_ORDINATIVO.eq(ORDINATIVO.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%").and(ORDINATIVO.ID_TIPO_RTS.ne(id99))))
                    .fetchOneInto(Integer.class);
                                    
            return Response.ok(count).build(); 
        }
        catch(DateTimeParseException dtpe) {
            log.error("Formato data non corretto {}", dtpe);
            
            return Response.serverError().entity("Formato data non corretto.").build();
        }
    } 
    
    @GET
    @Path("ordinativo/download/{id: \\d+}") 
    @Produces(MediaType.APPLICATION_JSON) 
    //@JWTVerify
    public Response getDownloadOrdinativo(@PathParam("id") int id) {
        Integer idAllegato = null;
        
        try {                    
            List<AllegatoRecord> allegati = ctx.selectFrom(ALLEGATO).where(ALLEGATO.ID_ORDINATIVO.eq(id)).fetch();        
            
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
    
    @GET
    @Path("modello/{tipo}") 
    @Produces(MediaType.APPLICATION_JSON) 
    //@JWTVerify
    public Response getModello(@PathParam("tipo") String tipo) {        
        try {                    
            List<ModelliRecord> modelli = ctx.selectFrom(MODELLI).where(MODELLI.TIPO.eq(tipo)).orderBy(MODELLI.DATA_PUBBLICAZIONE.desc()).fetch();
            
            if(modelli.isEmpty()) return Response.noContent().build();
            
            ModelliRecord modello = modelli.get(0);
            byte[] data = Files.readAllBytes(java.nio.file.Path.of(documentFolder+"/"+modello.getNomefileLocale()));
            Download d = new Download(modello.getNomefile(), data);
                        
            return Response.ok(d).build(); 
        }
        catch(IOException ioe) {
            log.error("Impossibile accedere al modello TIPO={}: {}", tipo, ioe);
            
            return Response.serverError().entity("Impossibile accedere al file.").build();
        }
    }
    
    @GET
    @Path("riepilogo/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getRiepilogo(@PathParam("from") String from, @PathParam("to") String to) {
        try {
            LocalDate ldFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate ldTo = LocalDate.parse(to, DateTimeFormatter.ofPattern("yyyyMMdd"));
        
            int anno = ldFrom.getYear();
            int numero = anno-2017+1;
            
            RiepilogoRGS r = new RiepilogoRGS(anno, numero, ldFrom, ldTo);
                        
            // Compilazione ENTRATE
            
            BigDecimal i = cs.getSaldoGeocos(ldFrom);                        
            r.getEntrate().setGiacenzaCassaInizioAnno(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(QUIETANZA.IMPORTO))
                    .from(QUIETANZA.join(TIPO_RTS).on(QUIETANZA.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(QUIETANZA.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("1")).and(DSL.upper(QUIETANZA.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);
            r.getEntrate().setFondiComunitari(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(QUIETANZA.IMPORTO))
                    .from(QUIETANZA.join(TIPO_RTS).on(QUIETANZA.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(QUIETANZA.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("2")).and(DSL.upper(QUIETANZA.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);
            r.getEntrate().setFondiStatali(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(QUIETANZA.IMPORTO))
                    .from(QUIETANZA.join(TIPO_RTS).on(QUIETANZA.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(QUIETANZA.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("3")).and(DSL.upper(QUIETANZA.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);
            r.getEntrate().setFondiRegionali(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(QUIETANZA.IMPORTO))
                    .from(QUIETANZA.join(TIPO_RTS).on(QUIETANZA.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(QUIETANZA.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("4")).and(DSL.upper(QUIETANZA.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class); 
            r.getEntrate().setFondiEntiLocali(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(QUIETANZA.IMPORTO))
                    .from(QUIETANZA.join(TIPO_RTS).on(QUIETANZA.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(QUIETANZA.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("5")).and(DSL.upper(QUIETANZA.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);
            r.getEntrate().setTariffeServizi(i!=null ? i : BigDecimal.ZERO);
                        
            i = ctx.select(DSL.sum(QUIETANZA.IMPORTO))
                    .from(QUIETANZA.join(TIPO_RTS).on(QUIETANZA.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(QUIETANZA.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("6")).and(DSL.upper(QUIETANZA.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);
            r.getEntrate().setAccensionePrestiti(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(QUIETANZA.IMPORTO))
                    .from(QUIETANZA.join(TIPO_RTS).on(QUIETANZA.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(QUIETANZA.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.in("7", "7GSE")).and(DSL.upper(QUIETANZA.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);
            r.getEntrate().setAltro(i!=null ? i : BigDecimal.ZERO);
            
            // Compilazione USCITE
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("A")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setRedditiDaLD(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("B")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setConsumiItermedi(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("C")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setInteressiPassivi(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("D")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setTrasferimentiARegioni(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("E")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setTrasferimentiaEL(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("F")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setInvestimentiDiretti(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("G")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setTrasferimentiContoCapitale(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("H")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setRimborsiPrestiti(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("I")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setVersamentiErariali(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("L")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setVersamentiPrevidenziali(i!=null ? i : BigDecimal.ZERO);
            
            i = ctx.select(DSL.sum(ORDINATIVO.IMPORTO))
                    .from(ORDINATIVO.join(TIPO_RTS).on(ORDINATIVO.ID_TIPO_RTS.eq(TIPO_RTS.ID)))
                    .where(ORDINATIVO.DATA_PAGAMENTO.between(ldFrom, ldTo)).and(TIPO_RTS.CODICE.eq("M")).and(DSL.upper(ORDINATIVO.NUMERO_PAGAMENTO).notLike("NO RAG%"))
                    .fetchOneInto(BigDecimal.class);                        
            r.getUscite().setAltro(i!=null ? i : BigDecimal.ZERO);
                        
            return Response.ok(r).build();   
        }
        catch(DateTimeParseException dtpe) {
            log.error("Formato data non corretto {}", dtpe);
            
            return Response.serverError().entity("Formato data non corretto.").build();
        }
        catch(DataAccessException e) {
            log.error("Errore durante la generazione del riepilogo da=[{}], a=[{}] - Ex: {}", from, to, e);
            
            return Response.serverError().entity("Errore durante la generazione del riepilogo.").build();
        }
    } 
    
    private List<OperaPubblica> getTotaliOOPP(String tipo, LocalDate from, LocalDate to, LocalDate toAp) {
        //int codice2 = ctx.select(TIPO_RTS.ID).from(TIPO_RTS).where(TIPO_RTS.CODICE.eq("2")).fetchSingleInto(Integer.class);
        //int codice7 = ctx.select(TIPO_RTS.ID).from(TIPO_RTS).where(TIPO_RTS.CODICE.eq("7")).fetchSingleInto(Integer.class);
        int codice2 = ctx.select(TIPO_RTS.ID).from(TIPO_RTS).where(TIPO_RTS.CODICE.eq("2")).fetchSingleInto(Integer.class);
        int codice7GSE = ctx.select(TIPO_RTS.ID).from(TIPO_RTS).where(TIPO_RTS.CODICE.eq("7GSE")).fetchSingleInto(Integer.class);
        int codice99 = ctx.select(TIPO_RTS.ID).from(TIPO_RTS).where(TIPO_RTS.CODICE.eq("99")).fetchSingleInto(Integer.class);
        
       /* String sql = """
                     SELECT ifnull(c.ordinanza, '') as ordinanza, 
                            ifnull(c.ente_diocesi, '') as ente, 
                            ifnull(c.provincia, '') as provincia, 
                            ifnull(c.descrizione, '') as intervento,
                            ifnull((select sum(q.importo) from quietanza q where q.id_codice = c.id and q.id_tipo_rts = {4} and q.data_pagamento between {0} and {1}), 0) as trasferito,
                            ((ifnull((select sum(o.importo) from ordinativo o where o.id_codice = c.id and o.data_pagamento between {0} and {1}), 0) - (ifnull((select sum(q.importo) from quietanza q where q.id_codice = c.id and q.id_tipo_rts = {5} and q.data_pagamento between {0} and {1}), 0)))) as liquidato,
                            ((ifnull((select sum(o.importo) from ordinativo o where o.id_codice = c.id and o.data_pagamento between {0} and {2}), 0) - (ifnull((select sum(q.importo) from quietanza q where q.id_codice = c.id and q.id_tipo_rts = {5} and q.data_pagamento between {0} and {2}), 0)))) as liquidato_ap
                     FROM usrbilancio.codice c where c.codice = {3} and c03 is not null
                     """;
       */
       
        String sql = """
                     SELECT 
                            concat(c.codice, '.', c.c01, '.', c.c02, '.', c.c03) as codice,
                            ifnull(c.ordinanza, '') as ordinanza, 
                            ifnull(c.ente_diocesi, '') as ente, 
                            ifnull(c.provincia, '') as provincia,  
                            ifnull(c.descrizione, '') as intervento,
                            ifnull((select sum(q.importo) from quietanza q where q.id_codice = c.id and q.id_tipo_rts in ({5}) and q.data_pagamento between {0} and {1}), 0) as trasferito,
                            ifnull((select sum(o.importo) from ordinativo o where o.id_codice = c.id and o.id_tipo_rts <> {4} and o.data_pagamento between {0} and {1}), 0) as liquidato,
                            ifnull((select sum(q.importo) from quietanza q where q.id_codice = c.id and q.id_tipo_rts <> {4} and q.id_tipo_rts not in ({5}) and q.data_pagamento between {0} and {1}), 0) as stornato,
                            ifnull((select sum(o.importo) from ordinativo o where o.id_codice = c.id and o.id_tipo_rts <> {4} and o.data_pagamento between {0} and {2}), 0) as liquidato_ap,
                            ifnull((select sum(q.importo) from quietanza q where q.id_codice = c.id and q.id_tipo_rts <> {4} and q.id_tipo_rts not in ({5}) and q.data_pagamento between {0} and {2}), 0) as stornato_ap
                     FROM usrbilancio.codice c where c.codice = {3} and c03 is not null
                     """;
        StringJoiner sj = new StringJoiner(",");
        sj.add(String.valueOf(codice2));
        sj.add(String.valueOf(codice7GSE));         
        //return ctx.fetch(sql, from, to, toAp, tipo, codice2, codice7).into(OperaPubblica.class);
        return ctx.fetch(sql, from, to, toAp, tipo, codice99, sj.toString()).into(OperaPubblica.class);
    }
     
    @GET
    @Path("oopp/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getOperePubbliche(@PathParam("from") String from, @PathParam("to") String to) {    
        LocalDate ldToAp = null;
        try {
            LocalDate ldFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate ldTo = LocalDate.parse(to, DateTimeFormatter.ofPattern("yyyyMMdd"));
            ldToAp = ldTo.minusYears(1);
            if(ldToAp.isBefore(ldFrom)) ldToAp = ldTo;
            
            List<OperaPubblica> lOp = getTotaliOOPP("POP", ldFrom, ldTo, ldToAp);
            
            return Response.ok(lOp).build();
        }
        catch(DateTimeParseException dtpe) {
            log.error("Formato data non corretto {}", dtpe);
            
            return Response.serverError().entity("Formato data non coreetto.").build();
        }
        catch(DataAccessException e) {
            log.error("Errore durante la generazione delle opere pubbliche da=[{}], a=[{}] ap=[{}] - Ex: {}", from, to, ldToAp);
            
            return Response.serverError().entity("Errore durante la generazione dell'elenco delle opere pubbliche.").build();
        }
    }
    
    @GET
    @Path("chiese/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
    //@JWTVerify
    public Response getChiese(@PathParam("from") String from, @PathParam("to") String to) {    
        LocalDate ldToAp = null;
        try {
            LocalDate ldFrom = LocalDate.parse(from, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate ldTo = LocalDate.parse(to, DateTimeFormatter.ofPattern("yyyyMMdd"));
            ldToAp = ldTo.minusYears(1);
            if(ldToAp.isBefore(ldFrom)) ldToAp = ldTo;
            
            List<OperaPubblica> lOp = getTotaliOOPP("PCH", ldFrom, ldTo, ldToAp);
            
            return Response.ok(lOp).build();
        }
        catch(DateTimeParseException dtpe) {
            log.error("Formato data non corretto {}", dtpe);
            
            return Response.serverError().entity("Formato data non coreetto.").build();
        }
        catch(DataAccessException e) {
            log.error("Errore durante la generazione delle chiese da=[{}], a=[{}] ap=[{}] - Ex: {}", from, to, ldToAp);
            
            return Response.serverError().entity("Errore durante la generazione dell'elenco delle chiese.").build();
        }
    }
    
    @GET
    @Path("ping/{msg}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping(@PathParam("msg") String msg) {
        try {
            String outMsg;          
            if(msg!=null && !msg.isEmpty()) {
                outMsg = ctx.fetchSingle("SELECT '%s';".formatted(msg)).into(String.class);
            }
            else {
                outMsg = ctx.fetchSingle("SELECT VERSION();").into(String.class);
            }
            
            return Response.ok(outMsg).build(); 
        } 
        catch(DataAccessException e) {
            return Response.serverError().entity("Errore PING: "+e.toString()).build();
        }
    }
    
    @GET
    @Path("ping")
    @Produces(MediaType.TEXT_PLAIN) 
    public Response ping() {
        return ping(null);
    }
} 
  