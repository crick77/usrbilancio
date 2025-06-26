/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.CapitoloRecord;
import it.usr.web.usrbilancio.interceptor.LogDatabaseOperation;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.jooq.DSLContext;
import org.jooq.SelectWhereStep;
import org.jooq.exception.DataAccessException;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CapitoloService {    
    @DSLBilancio
    @Inject
    DSLContext ctx;
    
    public List<CapitoloRecord> getCapitoli() {        
        return getCapitoli(false);
    }
    
    public List<CapitoloRecord> getCapitoli(boolean sort) { 
        SelectWhereStep s = ctx.selectFrom(Tables.CAPITOLO);
        return (sort ? s.orderBy(Tables.CAPITOLO.DESCRIZIONE.asc()) : s).fetch();
    }
    
    public List<CapitoloRecord> getCapitoliNuovoAnno() {        
        return ctx.selectFrom(Tables.CAPITOLO).where(Tables.CAPITOLO.NUOVOANNO.eq((byte)1)).fetch();
    }
    
    public CapitoloRecord getCapitolo(int id) {
        return ctx.selectFrom(Tables.CAPITOLO).where(Tables.CAPITOLO.ID.eq(id)).fetchOne();
    }
    
    @LogDatabaseOperation
    public void inserisci(CapitoloRecord cr) {
        try {
            ctx.insertInto(Tables.CAPITOLO).set(cr).execute();
        }
        catch(DataAccessException dae) {
            if(dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }
            
            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void modifica(CapitoloRecord cr) {
        try {
            long oldV = cr.getVersione();
            cr.setVersione(oldV+1);
            int updated = ctx.update(Tables.CAPITOLO).set(cr).where(Tables.CAPITOLO.ID.eq(cr.getId())).and(Tables.CAPITOLO.VERSIONE.eq(oldV)).execute();
            if(updated!=1) throw new StaleRecordException("Il record è stato già aggiornato.");
        }
        catch(DataAccessException dae) {
            if(dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }
            
            throw dae;            
        }
    }
    
    @LogDatabaseOperation
    public void elimina(CapitoloRecord cr) {
        try {
            int deleted = ctx.delete(Tables.CAPITOLO).where(Tables.CAPITOLO.ID.eq(cr.getId())).and(Tables.CAPITOLO.VERSIONE.eq(cr.getVersione())).execute();
            if(deleted!=1) throw new StaleRecordException("Il record è stato già eliminato.");
        }
        catch(DataAccessException dae) {
            if(dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IntegrityException(dae.getCause().getMessage());
            }
            
            throw dae;
        }
    }       
}
