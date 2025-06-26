/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.UtenteRecord;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.jooq.DSLContext;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class UtenteService {   
    @Inject
    @DSLBilancio
    DSLContext ctx;
        
    public UtenteRecord getUtente(String username) {
        return ctx.selectFrom(Tables.UTENTE).where(Tables.UTENTE.USERNAME.eq(username)).fetchOne();
    }
}
