/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoCodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.MimeTypeRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.interceptor.LogDatabaseOperation;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.model.Intervento;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import it.usr.web.usrbilancio.producer.DocumentFolder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CodiceService {               
    public enum GruppoRts {
        RTS_QUIETANZA, RTS_ORDINATIVO, RTS_TUTTI;
    }
    @DSLBilancio
    @Inject
    DSLContext ctx;
    @AppLogger
    @Inject
    Logger logger;
    @DocumentFolder
    @Inject
    String documentFolder;

    public List<CodiceRecord> getCodici() {
        return ctx.selectFrom(Tables.CODICE).orderBy(Tables.CODICE.CODICE_, Tables.CODICE.C01, Tables.CODICE.C02, Tables.CODICE.C03, Tables.CODICE.C04, Tables.CODICE.C05).fetch();
    }

    public Map<Integer, CodiceRecord> getCodiciAsMap() {
        List<CodiceRecord> codici = getCodici();
        Map<Integer, CodiceRecord> cMap = new HashMap<>();
        codici.forEach(c -> {
            cMap.put(c.getId(), c);
        });
        return cMap;
    }

    public CodiceRecord getCodiceById(int id) {
        return ctx.selectFrom(Tables.CODICE).where(Tables.CODICE.ID.eq(id)).fetchOne();
    }

    public CodiceRecord getCodiceByCodiceComposto(String cc) {
        return ctx.selectFrom(Tables.CODICE).where(DSL.concat(Tables.CODICE.CODICE_,
                DSL.coalesce(Tables.CODICE.C01, ""),
                DSL.coalesce(Tables.CODICE.C02, ""),
                DSL.coalesce(Tables.CODICE.C03, ""),
                DSL.coalesce(Tables.CODICE.C04, ""),
                DSL.coalesce(Tables.CODICE.C05, "")
        ).eq(cc)).fetchOne();
    }

    public List<TipoRtsRecord> getTipiRts(GruppoRts tipoRts) {
        return switch (tipoRts) {
            case RTS_ORDINATIVO -> ctx.selectFrom(Tables.TIPO_RTS).where(Tables.TIPO_RTS.CODICE.likeRegex("^[A-Z]+$")).or(Tables.TIPO_RTS.CODICE.in("99", "98")).orderBy(Tables.TIPO_RTS.CODICE).fetch();
            case RTS_QUIETANZA -> ctx.selectFrom(Tables.TIPO_RTS).where(Tables.TIPO_RTS.CODICE.likeRegex("^[0-9]+(?:[a-zA-Z]+)?$")).or(Tables.TIPO_RTS.CODICE.in("99", "98")).orderBy(Tables.TIPO_RTS.CODICE).fetch();
            default -> ctx.selectFrom(Tables.TIPO_RTS).orderBy(Tables.TIPO_RTS.CODICE).fetch();
        };
    }

    /*public Map<Integer, TipoRtsRecord> getTipiRtsAsMap(GruppoRts tipoRts) {
        List<TipoRtsRecord> rts = getTipiRts(tipoRts);
        Map<Integer, TipoRtsRecord> tMap = new HashMap<>();
        rts.forEach(t -> {
            tMap.put(t.getId(), t);
        });
        return tMap;
    }*/
    public TipoRtsRecord getTipoRtsById(int id) {
        return ctx.selectFrom(Tables.TIPO_RTS).where(Tables.TIPO_RTS.ID.eq(id)).fetchOne();
    }

    public List<TipoDocumentoRecord> getTipiDocumento() {
        return ctx.selectFrom(Tables.TIPO_DOCUMENTO).fetch();
    }

    public List<TipoDocumentoRecord> getTipiDocumentoNuovi() {
        return ctx.selectFrom(Tables.TIPO_DOCUMENTO).where(Tables.TIPO_DOCUMENTO.DESCRIZIONE.ne("DETERMINA")).fetch();
    }
    
    public Map<Integer, TipoDocumentoRecord> getTipiDocumentoAsMap() {
        List<TipoDocumentoRecord> rts = getTipiDocumento();
        Map<Integer, TipoDocumentoRecord> tMap = new HashMap<>();
        rts.forEach(t -> {
            tMap.put(t.getId(), t);
        });
        return tMap;
    }

    public TipoDocumentoRecord getTipoDocumentoById(int id) {
        return ctx.selectFrom(Tables.TIPO_DOCUMENTO).where(Tables.TIPO_DOCUMENTO.ID.eq(id)).fetchOne();
    }

    public TipoDocumentoRecord getTipoDocumentoByDescr(String desc) {
        return ctx.selectFrom(Tables.TIPO_DOCUMENTO).where(Tables.TIPO_DOCUMENTO.DESCRIZIONE.like(desc + "%")).fetchOne();
    }

    public TipoRtsRecord getTipoRtsByCodice(String descr) {
        return ctx.selectFrom(Tables.TIPO_RTS).where(Tables.TIPO_RTS.CODICE.eq(descr)).fetchOne();
    }

    public String getMimeType(String mime) {
        if (mime == null) {
            return null;
        }

        MimeTypeRecord r = ctx.selectFrom(Tables.MIME_TYPE).where(Tables.MIME_TYPE.MIME.eq(mime)).fetchAny();
        return (r != null) ? r.getDescrizione() : mime;
    }
   
    @LogDatabaseOperation
    public void inserisci(CodiceRecord cr) {
        try {
            ctx.insertInto(Tables.CODICE).set(cr).execute();
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void modifica(CodiceRecord cr) {
        try {
            ctx.transaction(trx -> {
                Condition cond = DSL.noCondition();
                cond = cond.and(Tables.CODICE.CODICE_.eq(cr.getCodice())).and(Tables.CODICE.ID.notEqual(cr.getId()));
                if(cr.getC01()!=null) cond = cond.and(Tables.CODICE.C01.eq(cr.getC01())); else cond = cond.and(Tables.CODICE.C01.isNull());
                if(cr.getC02()!=null) cond = cond.and(Tables.CODICE.C02.eq(cr.getC02())); else cond = cond.and(Tables.CODICE.C02.isNull());
                if(cr.getC03()!=null) cond = cond.and(Tables.CODICE.C03.eq(cr.getC03())); else cond = cond.and(Tables.CODICE.C03.isNull());
                if(cr.getC04()!=null) cond = cond.and(Tables.CODICE.C04.eq(cr.getC04())); else cond = cond.and(Tables.CODICE.C04.isNull());
                if(cr.getC05()!=null) cond = cond.and(Tables.CODICE.C05.eq(cr.getC05())); else cond = cond.and(Tables.CODICE.C05.isNull());
                
                int count = trx.dsl().select(DSL.count()).from(Tables.CODICE).where(cond).fetchSingleInto(Integer.class);
                if(count>0) {
                    throw new DuplicationException("Esiste già un record con i codici indicati.");
                }
                
                int updated = trx.dsl().update(Tables.CODICE).set(cr).where(Tables.CODICE.ID.eq(cr.getId())).execute();
                if (updated != 1) {
                    throw new StaleRecordException("Il record che si stava modificando è stato eliminato.");
                }
            });
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void elimina(CodiceRecord cr) {
        try {
            int deleted = ctx.delete(Tables.CODICE).where(Tables.CODICE.ID.eq(cr.getId())).execute();
            if (deleted != 1) {
                throw new StaleRecordException("Il record è stato già eliminato.");
            }
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IntegrityException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }
        
    @LogDatabaseOperation
    public void inserisci(TipoRtsRecord rts) {
        try {
            ctx.insertInto(Tables.TIPO_RTS).set(rts).execute();
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException("Il codice RTS indicato esiste già.");
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void modifica(TipoRtsRecord rts) {
        try {
            ctx.transaction(trx -> {
                int count = trx.dsl().select(DSL.count()).from(Tables.TIPO_RTS).where(Tables.TIPO_RTS.CODICE.eq(rts.getCodice())).and(Tables.TIPO_RTS.ID.notEqual(rts.getId())).fetchSingleInto(Integer.class);
                if(count>0) {
                    throw new DuplicationException("Esiste già un record con il codice RTS indicato.");
                }
                
                int updated = trx.dsl().update(Tables.TIPO_RTS).set(rts).where(Tables.TIPO_RTS.ID.eq(rts.getId())).execute();
                if (updated != 1) {
                    throw new StaleRecordException("Il record che si stava modificando è stato eliminato.");
                }
            });
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void elimina(TipoRtsRecord rts) {
        try {
            int deleted = ctx.delete(Tables.TIPO_RTS).where(Tables.TIPO_RTS.ID.eq(rts.getId())).execute();
            if (deleted != 1) {
                throw new StaleRecordException("Il record è stato già eliminato.");
            }
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IntegrityException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }
    
    public List<Intervento> getInterventiPubblica() {
        return null;
    }
    
    public CodiceRecord cercaUltimo(CodiceRecord cod) {
        BigDecimal limitOOPP = new BigDecimal(5000);
        Condition cond = DSL.noCondition();
        cond = cond.and(Tables.CODICE.CODICE_.eq(cod.getCodice()));
        // chiese
        if("PUB".equalsIgnoreCase(cod.getCodice()) && cod.getC01()==null) {
            cond = cond.and(Tables.CODICE.C03.cast(SQLDataType.NUMERIC).lessThan(limitOOPP));
            OrderField<?>[] ordinamento = {Tables.CODICE.C03.cast(SQLDataType.NUMERIC).desc()};

            List<CodiceRecord> elenco = ctx.selectFrom(Tables.CODICE).where(cond).orderBy(ordinamento).limit(1).fetch();
            return elenco.isEmpty() ? null : elenco.get(0);
        }
        // oopp
        if("PUB".equalsIgnoreCase(cod.getCodice()) && "01".equalsIgnoreCase(cod.getC01())) {
            cond = cond.and(Tables.CODICE.C03.cast(SQLDataType.NUMERIC).ge(limitOOPP));
            OrderField<?>[] ordinamento = {Tables.CODICE.C03.cast(SQLDataType.NUMERIC).desc()};

            List<CodiceRecord> elenco = ctx.selectFrom(Tables.CODICE).where(cond).orderBy(ordinamento).limit(1).fetch();
            return elenco.isEmpty() ? null : elenco.get(0);
        }        
        else { // tutto il resto
            if(cod.getC01()!=null) {
                cond = cond.and(Tables.CODICE.C01.eq(cod.getC01()));
            }
            else {
                cond = cond.and(Tables.CODICE.C01.isNotNull());
            }

            int[] toDo = {1, 2, 2, 2}; // 0 = usa valore, 1 = not is null, 2 = is null

            if(cod.getC04()!=null) {
                toDo[3] = 1;
                toDo[2] = toDo[1] = toDo[0] = 0;
            }
            if(cod.getC03()!=null) {
                toDo[2] = 1;
                toDo[1] = toDo[0] = 0; 
            } 
            if(cod.getC02()!=null) {
                toDo[1] = 1;
                toDo[0] = 0; 
            } 

            for(int i=0;i<toDo.length;i++) {
                switch(i) {
                    case 0 -> {
                        switch(toDo[i]) {
                            case 0 -> { cond = cond.and(Tables.CODICE.C02.eq(cod.getC02())); }
                            case 1 -> { cond = cond.and(Tables.CODICE.C02.isNotNull()); }
                            case 2 -> { cond = cond.and(Tables.CODICE.C02.isNull()); }                        
                        }
                    }
                    case 1 -> {
                        switch(toDo[i]) {
                            case 0 -> { cond = cond.and(Tables.CODICE.C03.eq(cod.getC03())); }
                            case 1 -> { cond = cond.and(Tables.CODICE.C03.isNotNull()); }
                            case 2 -> { cond = cond.and(Tables.CODICE.C03.isNull()); }                        
                        }
                    }
                    case 2 -> {
                        switch(toDo[i]) {
                            case 0 -> { cond = cond.and(Tables.CODICE.C04.eq(cod.getC04())); }
                            case 1 -> { cond = cond.and(Tables.CODICE.C04.isNotNull()); }
                            case 2 -> { cond = cond.and(Tables.CODICE.C04.isNull()); }                        
                        }
                    }
                    case 3 -> {
                        switch(toDo[i]) {
                            case 0 -> { cond = cond.and(Tables.CODICE.C05.eq(cod.getC05())); }
                            case 1 -> { cond = cond.and(Tables.CODICE.C05.isNotNull()); }
                            case 2 -> { cond = cond.and(Tables.CODICE.C05.isNull()); }                        
                        }
                    }
                }            
            }

            OrderField<?>[] ordinamento = {Tables.CODICE.CODICE_.desc(), Tables.CODICE.C01.cast(SQLDataType.NUMERIC).desc(), Tables.CODICE.C02.cast(SQLDataType.NUMERIC).desc(),
                                           Tables.CODICE.C03.cast(SQLDataType.NUMERIC).desc(), Tables.CODICE.C04.cast(SQLDataType.NUMERIC).desc(), Tables.CODICE.C05.cast(SQLDataType.NUMERIC).desc()};

            List<CodiceRecord> elenco = ctx.selectFrom(Tables.CODICE).where(cond).orderBy(ordinamento).limit(1).fetch();
            return elenco.isEmpty() ? null : elenco.get(0);
        }
    } 
    
    public List<AllegatoCodiceRecord> getAllegatiCodice(Integer idCodice) {
        return ctx.selectFrom(Tables.ALLEGATO_CODICE).where(Tables.ALLEGATO_CODICE.ID_CODICE.eq(idCodice)).fetch();
    }
    
    public AllegatoCodiceRecord getAllegatoCodiceById(int idAllegatoCodice) {
        return ctx.selectFrom(Tables.ALLEGATO_CODICE).where(Tables.ALLEGATO_CODICE.ID.eq(idAllegatoCodice)).fetchOne();
    }
    
    @LogDatabaseOperation
    public void inserisci(List<Documento> documenti, Integer idCodice) {
        List<AllegatoCodiceRecord> all = new ArrayList<>();
 
        try {
            ctx.transaction(trx -> {
                try {
                    for (Documento doc : documenti) {
                        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
                        Files.write(Paths.get(documentFolder + "/" + localFileName), doc.getContent());
                        logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", localFileName, doc.getFileName());

                        AllegatoCodiceRecord a = new AllegatoCodiceRecord(null, idCodice, doc.getGruppo(), doc.getFileName(), localFileName, doc.getContentType());                        
                        trx.dsl().insertInto(Tables.ALLEGATO_CODICE).set(a).execute();
                        all.add(a);
                    } 
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }
            });
        } 
        catch (DataAccessException dae) {
            for (AllegatoCodiceRecord a : all) {
                try {
                    if (a.getNomefileLocale() != null) {
                        Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
                        logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", a.getNomefileLocale());
                    }
                } catch (IOException e) {
                }
            }

            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void modifica(AllegatoCodiceRecord allegato) {           
        ctx.update(Tables.ALLEGATO_CODICE).set(allegato).where(Tables.ALLEGATO_CODICE.ID.eq(allegato.getId())).execute();   
    }
    
    @LogDatabaseOperation
    public void elimina(AllegatoCodiceRecord allegato) {
        ctx.transaction(trx -> {
            int rem = trx.dsl().deleteFrom(Tables.ALLEGATO_CODICE).where(Tables.ALLEGATO_CODICE.ID.eq(allegato.getId())).execute();
            if (rem != 1) {
                throw new StaleRecordException("L'allegato [" + allegato.getId() + "] è stato già eliminato.");
            }

            try {
                Files.delete(Paths.get(documentFolder + "/" + allegato.getNomefileLocale()));
                logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", allegato.getNomefileLocale());
            } 
            catch (IOException e) {
                logger.warn("Errore nell'eliminazione del file per l'allegato id [{}] nomefile [{}]. Il record è stato eliminato. Errore: {}", allegato.getId(), allegato.getNomefileLocale(), e);
            }
        });
    }
}
