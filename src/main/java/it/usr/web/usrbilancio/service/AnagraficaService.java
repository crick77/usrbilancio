/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import static it.usr.web.usrbilancio.domain.Tables.*;
import it.usr.web.usrbilancio.domain.tables.records.AnagraficaRecord;
import it.usr.web.usrbilancio.domain.tables.records.ComuneRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoRecord;
import it.usr.web.usrbilancio.domain.tables.records.RitenutaRecord;
import it.usr.web.usrbilancio.interceptor.LogDatabaseOperation;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/** 
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class AnagraficaService {
    @DSLBilancio
    @Inject
    DSLContext ctx;

    public List<AnagraficaRecord> getAnagrafica() {
        return ctx.selectFrom(ANAGRAFICA).fetch();
    }
 
    public AnagraficaRecord getAnagraficaById(int id) {
        return ctx.selectFrom(ANAGRAFICA).where(ANAGRAFICA.ID.eq(id)).fetchOne();  
    }
    
    public AnagraficaRecord getAnagraficaOrdinativo(OrdinativoRecord or) {
        return ctx.select().from(ANAGRAFICA).join(RITENUTA).on(ANAGRAFICA.ID.eq(RITENUTA.ID_ANAGRAFICA)).where(RITENUTA.ID_ORDINATIVO.eq(or.getId())).fetchOneInto(AnagraficaRecord.class);
    }
            
    @LogDatabaseOperation
    public void inserisci(AnagraficaRecord ar) { 
        try {
            Condition cond = DSL.noCondition();
            if (notEmpty(ar.getCf())) {
                cond = cond.and(ANAGRAFICA.CF.eq(ar.getCf()));
            }
            if (notEmpty(ar.getPiva())) {
                cond = cond.or(ANAGRAFICA.PIVA.eq(ar.getPiva()));
            }
            AnagraficaRecord _ar = ctx.selectFrom(ANAGRAFICA).where(cond).fetchAny();

            if (_ar != null) {
                throw new DuplicationException("Anagrafica già presente.");
            }

            ctx.insertInto(ANAGRAFICA).set(ar).execute();
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void modifica(AnagraficaRecord ar) {
        try {
            ctx.transaction(trx -> {
                Condition cond = DSL.noCondition();
                if (notEmpty(ar.getCf())) {
                    cond = cond.and(ANAGRAFICA.CF.eq(ar.getCf()));
                }
                if (notEmpty(ar.getPiva())) {
                    cond = cond.or(ANAGRAFICA.PIVA.eq(ar.getPiva()));
                }
                cond = cond.and(ANAGRAFICA.ID.ne(ar.getId()));

                AnagraficaRecord _ar = trx.dsl().selectFrom(ANAGRAFICA).where(cond).fetchAny();

                if (_ar != null) {
                    throw new DuplicationException("Anagrafica già presente.");
                }

                long oldV = ar.getVersione();
                ar.setVersione(oldV+1);
                int upd = trx.dsl().update(ANAGRAFICA).set(ar).where(ANAGRAFICA.ID.eq(ar.getId()).and(ANAGRAFICA.VERSIONE.eq(oldV))).execute();

                if(upd==0) throw new StaleRecordException("Il record è stato già modificato o eliminato");
            });
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void elimina(AnagraficaRecord ar) {        
        try {        
            int upd = ctx.deleteFrom(ANAGRAFICA).where(ANAGRAFICA.ID.eq(ar.getId()).and(ANAGRAFICA.VERSIONE.eq(ar.getVersione()))).execute();
            if(upd==0) throw new StaleRecordException("Il record è stato già modificato o eliminato");
        }
        catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IntegrityException(dae.getCause().getMessage());
            }

            throw dae;
        }
        
    }

    public boolean notEmpty(String s) {
        return s!=null && !s.isEmpty();
    }

    public RitenutaRecord getRitenuta(Integer idOrd, Integer idAna) {
        return ctx.selectFrom(RITENUTA).where(RITENUTA.ID_ORDINATIVO.eq(idOrd).and(RITENUTA.ID_ANAGRAFICA.eq(idAna))).fetchOne();
    }   
    
    public List<ComuneRecord> getComuni() {
        return ctx.selectFrom(COMUNE).orderBy(COMUNE.COMUNE_).fetch();
    }
     
    public ComuneRecord getComune(String comune) {       
        return ctx.selectFrom(COMUNE).where(DSL.upper(COMUNE.COMUNE_).eq(comune)).fetchOne();
    }
    
    @LogDatabaseOperation
    public void inserisci(RitenutaRecord rr) { 
        try {            
            ctx.insertInto(RITENUTA).set(rr).execute();
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void modifica(RitenutaRecord rr) {
        try {
            long oldV = rr.getVersione();
            rr.setVersione(oldV+1);
            int upd = ctx.update(RITENUTA).set(rr).where(RITENUTA.ID.eq(rr.getId()).and(RITENUTA.VERSIONE.eq(oldV))).execute();

            if(upd==0) throw new StaleRecordException("Il record è stato già modificato o eliminato");

        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void elimina(RitenutaRecord rr) {        
        try {        
            int upd = ctx.deleteFrom(RITENUTA).where(RITENUTA.ID.eq(rr.getId()).and(RITENUTA.VERSIONE.eq(rr.getVersione()))).execute();
            if(upd==0) throw new StaleRecordException("Il record è stato già modificato o eliminato");
        }
        catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IntegrityException(dae.getCause().getMessage());
            }

            throw dae;
        }        
    }
}
