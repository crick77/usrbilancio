/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.ContabilitaRecord;
import it.usr.web.usrbilancio.domain.tables.records.RichiestaRecord;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class RichiestaService {
    @Inject
    @AppLogger
    Logger logger;
    @DSLBilancio
    @Inject
    DSLContext ctx;
    
    public List<RichiestaRecord> getRichieste(ContabilitaRecord contabilita) {
        return ctx.select(Tables.RICHIESTA)                
                .from(Tables.RICHIESTA).join(Tables.COMPETENZA).on(Tables.RICHIESTA.ID_COMPETENZA.eq(Tables.COMPETENZA.ID)).join(Tables.CAPITOLO).on(Tables.COMPETENZA.ID_CAPITOLO.eq(Tables.CAPITOLO.ID))
                .where(Tables.CAPITOLO.ID_CONTABILITA.eq(contabilita.getId()))
                .fetchInto(Tables.RICHIESTA);
    }
    
    public List<RichiestaRecord> getRichiesteByCompetenza(int idCompetenza) {
        return ctx.selectFrom(Tables.RICHIESTA).where(Tables.RICHIESTA.ID_COMPETENZA.eq(idCompetenza)).fetch();
    }
    
    public BigDecimal getTotaleRichieste(int idCompetenza) {
        return ctx.select(DSL.sum(Tables.RICHIESTA.IMPORTO.times(DSL.if_(Tables.RICHIESTA.INGRESSO.eq((byte)1), (byte)1, (byte)-1)))).from(Tables.RICHIESTA).where(Tables.RICHIESTA.ID_COMPETENZA.eq(idCompetenza)).fetchSingle().value1();
    }
}
  