/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.RettificaIvaRecord;
import it.usr.web.usrbilancio.interceptor.LogDatabaseOperation;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RettificaIvaService {
    @DSLBilancio
    @Inject
    DSLContext ctx;
    @AppLogger
    @Inject
    Logger logger;
    
    public List<RettificaIvaRecord> getRettificheIVA() {
        return ctx.selectFrom(Tables.RETTIFICA_IVA).orderBy(Tables.RETTIFICA_IVA.ANNO).fetch();
    }
    
    public RettificaIvaRecord getRettificaIVAAnno(int anno) {
        return ctx.selectFrom(Tables.RETTIFICA_IVA).where(Tables.RETTIFICA_IVA.ANNO.eq(anno)).fetchOne();
    }
    
    @LogDatabaseOperation
    public void inserisci(RettificaIvaRecord ri) {
        try {
            ctx.insertInto(Tables.RETTIFICA_IVA).set(ri).execute();
        }
        catch(DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }
            
            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void modifica(int anno, RettificaIvaRecord ri) {
        try {
            int mod = ctx.update(Tables.RETTIFICA_IVA).set(ri).where(Tables.RETTIFICA_IVA.ANNO.eq(anno)).execute();
            if (mod != 1) {
                throw new StaleRecordException("La rettifica IVA per l'anno [" + anno + "] è stata già eliminata da un altro utente.");
            }
        }
        catch(DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }
            
            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void elimina(RettificaIvaRecord ri) {
        try {
            int rem = ctx.delete(Tables.RETTIFICA_IVA).where(Tables.RETTIFICA_IVA.ANNO.eq(ri.getAnno())).execute();
            if (rem != 1) {
                throw new StaleRecordException("La rettifica IVA per l'anno [" + ri.getAnno()+ "] è stata già eliminata da un altro utente.");
            }
        }
        catch(DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }
            
            throw dae;
        }
    }
}
