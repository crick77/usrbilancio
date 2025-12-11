/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import static it.usr.web.usrbilancio.domain.Tables.*;
import it.usr.web.usrbilancio.domain.tables.records.LogOperazioniRecord;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import org.jooq.DSLContext;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
public class LogOperazioniService {
    @DSLBilancio
    @Inject
    DSLContext ctx;
    
    public List<LogOperazioniRecord> getLogOperazioni() {
        return ctx.selectFrom(LOG_OPERAZIONI).orderBy(LOG_OPERAZIONI.DATA_ORA.desc()).fetch();
    }       
    
    public List<String> getServizi() {
        return ctx.selectDistinct(LOG_OPERAZIONI.SERVICE).from(LOG_OPERAZIONI).fetchInto(String.class);
    }
    
    public List<String> getOperatori() {
        return ctx.selectDistinct(LOG_OPERAZIONI.OPERATORE).from(LOG_OPERAZIONI).fetchInto(String.class);
    }
}
