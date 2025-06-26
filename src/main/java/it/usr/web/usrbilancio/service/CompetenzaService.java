/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.domain.ActiveUser;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.CompetenzaRecord;
import it.usr.web.usrbilancio.domain.tables.records.LogOperazioniRecord;
import it.usr.web.usrbilancio.domain.tables.records.RichiestaRecord;
import it.usr.web.usrbilancio.interceptor.LogDatabaseOperation;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.model.StatoCapitolo;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import it.usr.web.usrbilancio.producer.DocumentFolder;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CompetenzaService {

    @DSLBilancio
    @Inject
    DSLContext ctx;
    @Inject
    ActiveUser user;
    @Inject
    @DocumentFolder
    String documentFolder;
    @Inject
    @AppLogger
    Logger logger;

    public List<CompetenzaRecord> getCompetenzeCapitolo(int idCapitolo) {
        return ctx.selectFrom(Tables.COMPETENZA).where(Tables.COMPETENZA.ID_CAPITOLO.eq(idCapitolo)).orderBy(Tables.COMPETENZA.ANNO).fetch();
    }

    @LogDatabaseOperation
    public void inserisci(CompetenzaRecord cr) {
        try {
            ctx.insertInto(Tables.COMPETENZA).set(cr).execute();
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void modifica(CompetenzaRecord cr) {
        try {
            long oldV = cr.getVersione();
            cr.setVersione(oldV + 1);
            int updated = ctx.update(Tables.COMPETENZA).set(cr).where(Tables.COMPETENZA.ID.eq(cr.getId())).and(Tables.COMPETENZA.VERSIONE.eq(oldV)).execute();
            if (updated != 1) {
                throw new StaleRecordException("Il record che si stava modificando è stato eliminato.");
            }
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void elimina(CompetenzaRecord cr) {
        try {
            int deleted = ctx.delete(Tables.COMPETENZA).where(Tables.COMPETENZA.ID.eq(cr.getId())).and(Tables.COMPETENZA.VERSIONE.eq(cr.getVersione())).execute();
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

    public List<CapitoloCompetenza> getCapitoliCompetenze() {
        return ctx.select(Tables.COMPETENZA.ID, Tables.COMPETENZA.ID_CAPITOLO, Tables.CAPITOLO.DESCRIZIONE, Tables.CAPITOLO.STANZIAMENTO, Tables.CAPITOLO.NUOVOANNO, Tables.COMPETENZA.ANNO, Tables.COMPETENZA.CHIUSO, Tables.CAPITOLO.DACONSOLIDARE)
                .from(Tables.CAPITOLO).innerJoin(Tables.COMPETENZA).on(Tables.CAPITOLO.ID.eq(Tables.COMPETENZA.ID_CAPITOLO))
                .orderBy(Tables.CAPITOLO.DESCRIZIONE, Tables.COMPETENZA.ANNO.desc())
                .fetch().into(CapitoloCompetenza.class);
    }

    public List<CapitoloCompetenza> getCapitoliCompetenzeAperti() {
        return ctx.select(Tables.COMPETENZA.ID, Tables.COMPETENZA.ID_CAPITOLO, Tables.CAPITOLO.DESCRIZIONE, Tables.CAPITOLO.STANZIAMENTO, Tables.CAPITOLO.NUOVOANNO, Tables.COMPETENZA.ANNO, Tables.COMPETENZA.CHIUSO, Tables.CAPITOLO.DACONSOLIDARE)
                .from(Tables.CAPITOLO).innerJoin(Tables.COMPETENZA).on(Tables.CAPITOLO.ID.eq(Tables.COMPETENZA.ID_CAPITOLO))
                .where(Tables.COMPETENZA.CHIUSO.eq((byte) 0))
                .orderBy(Tables.CAPITOLO.DESCRIZIONE, Tables.COMPETENZA.ANNO.desc())
                .fetch().into(CapitoloCompetenza.class);
    }

    public List<CapitoloCompetenza> getCapitoliCompetenzeApertiNonFuturi() {
        return ctx.select(Tables.COMPETENZA.ID, Tables.COMPETENZA.ID_CAPITOLO, Tables.CAPITOLO.DESCRIZIONE, Tables.CAPITOLO.STANZIAMENTO, Tables.CAPITOLO.NUOVOANNO, Tables.COMPETENZA.ANNO, Tables.COMPETENZA.CHIUSO, Tables.CAPITOLO.DACONSOLIDARE)
                .from(Tables.CAPITOLO).innerJoin(Tables.COMPETENZA).on(Tables.CAPITOLO.ID.eq(Tables.COMPETENZA.ID_CAPITOLO))
                .where(Tables.COMPETENZA.CHIUSO.eq((byte) 0)).and(Tables.COMPETENZA.ANNO.lessOrEqual(LocalDate.now().getYear()))
                .orderBy(Tables.CAPITOLO.DESCRIZIONE, Tables.COMPETENZA.ANNO.desc())
                .fetch().into(CapitoloCompetenza.class);
    }

    public List<CapitoloCompetenza> getCapitoliCompetenze(int anno) {
        return ctx.select(Tables.COMPETENZA.ID, Tables.COMPETENZA.ID_CAPITOLO, Tables.CAPITOLO.DESCRIZIONE, Tables.CAPITOLO.STANZIAMENTO, Tables.CAPITOLO.NUOVOANNO, Tables.COMPETENZA.ANNO, Tables.COMPETENZA.CHIUSO, Tables.CAPITOLO.DACONSOLIDARE)
                .from(Tables.CAPITOLO).innerJoin(Tables.COMPETENZA).on(Tables.CAPITOLO.ID.eq(Tables.COMPETENZA.ID_CAPITOLO))
                .where(Tables.COMPETENZA.ANNO.eq(anno))
                .orderBy(Tables.CAPITOLO.DESCRIZIONE, Tables.COMPETENZA.ANNO.desc())
                .fetch().into(CapitoloCompetenza.class);
    }

    public List<CapitoloCompetenza> getCapitoliCompetenze(boolean nuovoAnno, int anno) {
        byte na = (byte) (nuovoAnno ? 1 : 0);
        return ctx.select(Tables.COMPETENZA.ID, Tables.COMPETENZA.ID_CAPITOLO, Tables.CAPITOLO.DESCRIZIONE, Tables.CAPITOLO.STANZIAMENTO, Tables.CAPITOLO.NUOVOANNO, Tables.COMPETENZA.ANNO, Tables.COMPETENZA.CHIUSO, Tables.CAPITOLO.DACONSOLIDARE)
                .from(Tables.CAPITOLO).innerJoin(Tables.COMPETENZA).on(Tables.CAPITOLO.ID.eq(Tables.COMPETENZA.ID_CAPITOLO))
                .where(Tables.COMPETENZA.ANNO.eq(anno)).and(Tables.CAPITOLO.NUOVOANNO.eq(na))
                .orderBy(Tables.CAPITOLO.DESCRIZIONE, Tables.COMPETENZA.ANNO.desc())
                .fetch().into(CapitoloCompetenza.class);
    }

    public List<CapitoloCompetenza> getCapitoliCompetenze(int anno, boolean chiusi) {
        return ctx.select(Tables.COMPETENZA.ID, Tables.COMPETENZA.ID_CAPITOLO, Tables.CAPITOLO.DESCRIZIONE, Tables.CAPITOLO.STANZIAMENTO, Tables.CAPITOLO.NUOVOANNO, Tables.COMPETENZA.ANNO, Tables.COMPETENZA.CHIUSO, Tables.CAPITOLO.DACONSOLIDARE)
                .from(Tables.CAPITOLO).innerJoin(Tables.COMPETENZA).on(Tables.CAPITOLO.ID.eq(Tables.COMPETENZA.ID_CAPITOLO))
                .where(Tables.COMPETENZA.ANNO.eq(anno).and(Tables.COMPETENZA.CHIUSO.eq((byte) (chiusi ? 1 : 0))))
                .orderBy(Tables.CAPITOLO.DESCRIZIONE, Tables.COMPETENZA.ANNO.desc())
                .fetch().into(CapitoloCompetenza.class);
    }

    public CapitoloCompetenza getCapitoloCompetenzaById(int idCompetenza) {
        return ctx.select(Tables.COMPETENZA.ID, Tables.COMPETENZA.ID_CAPITOLO, Tables.CAPITOLO.DESCRIZIONE, Tables.CAPITOLO.STANZIAMENTO, Tables.CAPITOLO.NUOVOANNO, Tables.COMPETENZA.ANNO, Tables.COMPETENZA.CHIUSO, Tables.CAPITOLO.DACONSOLIDARE)
                .from(Tables.CAPITOLO).innerJoin(Tables.COMPETENZA).on(Tables.CAPITOLO.ID.eq(Tables.COMPETENZA.ID_CAPITOLO))
                .where(Tables.COMPETENZA.ID.eq(idCompetenza))
                .fetchSingle().into(CapitoloCompetenza.class);
    }

    public CapitoloCompetenza getCapitoloCompetenzaByIdCapitoloAnno(int idCompetenza, int anno) {
        CompetenzaRecord cr = ctx.selectFrom(Tables.COMPETENZA).where(Tables.COMPETENZA.ID.eq(idCompetenza)).fetchOne();
        if (cr != null) {
            return ctx.select(Tables.COMPETENZA.ID, Tables.COMPETENZA.ID_CAPITOLO, Tables.CAPITOLO.DESCRIZIONE, Tables.CAPITOLO.STANZIAMENTO, Tables.CAPITOLO.NUOVOANNO, Tables.COMPETENZA.ANNO, Tables.COMPETENZA.CHIUSO, Tables.CAPITOLO.DACONSOLIDARE)
                    .from(Tables.CAPITOLO).innerJoin(Tables.COMPETENZA).on(Tables.CAPITOLO.ID.eq(Tables.COMPETENZA.ID_CAPITOLO))
                    .where(Tables.CAPITOLO.ID.eq(cr.getIdCapitolo()).and(Tables.COMPETENZA.ANNO.eq(anno)))
                    .fetchSingle().into(CapitoloCompetenza.class);
        }
        return null;
    }

    public double getSaldoCapitoloCompetenza(int idCompetenza) {
        Table<?> ordinativi = ctx.select(DSL.sum(Tables.ORDINATIVO.IMPORTO).mul(DSL.val(-1)).as("s")).from(Tables.ORDINATIVO).where(Tables.ORDINATIVO.ID_COMPETENZA.eq(idCompetenza)).asTable("o");
        Table<?> quietanze = ctx.select(DSL.sum(Tables.QUIETANZA.IMPORTO).as("s")).from(Tables.QUIETANZA).where(Tables.QUIETANZA.ID_COMPETENZA.eq(idCompetenza)).asTable("q");
        Table<?> virtuali = ctx.select(DSL.sum(Tables.MOVIMENTI_VIRTUALI.IMPORTO).as("s")).from(Tables.MOVIMENTI_VIRTUALI).where(Tables.MOVIMENTI_VIRTUALI.ID_COMPETENZA.eq(idCompetenza)).asTable("v");

        Table<?> r = ctx.select(ordinativi.fields()).from(ordinativi).union(
                ctx.select(quietanze.fields()).from(quietanze)).union(
                ctx.select(virtuali.fields()).from(virtuali)).asTable("t");

        Record out = ctx.select(DSL.sum(r.field("s", BigDecimal.class))).from(r).fetchSingle();
        return out.get(0, BigDecimal.class).doubleValue();
    }

    public List<StatoCapitolo> getSituazione(Integer anno) {
        String sql = "select comp.id, cap.descrizione, comp.anno, comp.chiuso, "
                + "ifnull((select sum(o.importo) from ordinativo o where o.id_competenza = comp.id), 0) as importo_ordinativi, "
                + "ifnull((select sum(q.importo) from quietanza q where q.id_competenza = comp.id), 0) as importo_quietanze, "
                + "ifnull((select sum(mv.importo) from movimenti_virtuali mv where mv.id_competenza = comp.id), 0) as importo_virtuale "
                + "from competenza as comp inner join capitolo cap on comp.id_capitolo = cap.id "
                + ((anno != null) ? "where comp.anno = " + anno + " " : "")
                + "order by comp.anno, cap.descrizione";
        return ctx.fetch(sql).into(StatoCapitolo.class);
    }

    public BigDecimal getSaldoVirtualeChiusi() {
        String sql = "select sum(ifnull((select sum(q.importo) from quietanza q where q.id_competenza = comp.id), 0)) - "
                + "sum(ifnull((select sum(o.importo) from ordinativo o where o.id_competenza = comp.id), 0)) + "
                + "sum(ifnull((select sum(mv.importo) from movimenti_virtuali mv where mv.id_competenza = comp.id), 0)) as saldo_virtuale_chiusi "
                + "from competenza as comp inner join capitolo cap on comp.id_capitolo = cap.id "
                + "where comp.chiuso = 1";
        return ctx.fetchSingle(sql).into(BigDecimal.class);
    }

    public List<StatoCapitolo> getSituazioneAperti() {
        String sql = "select comp.id, cap.descrizione, comp.anno, comp.chiuso, "
                + "ifnull((select sum(o.importo) from ordinativo o where o.id_competenza = comp.id), 0) as importo_ordinativi, "
                + "ifnull((select sum(q.importo) from quietanza q where q.id_competenza = comp.id), 0) as importo_quietanze, "
                + "ifnull((select sum(mv.importo) from movimenti_virtuali mv where mv.id_competenza = comp.id), 0) as importo_virtuale "
                + "from competenza as comp inner join capitolo cap on comp.id_capitolo = cap.id "
                + "where comp.chiuso in (0,2) "
                + "order by comp.anno, cap.descrizione";
        return ctx.fetch(sql).into(StatoCapitolo.class);
    }

    public BigDecimal getSaldoGeocos(LocalDate dataSaldo) {
        String sql = "select (select sum(q.importo) from quietanza q where q.data_pagamento <= {0})-(select sum(o.importo) from ordinativo o where o.data_pagamento <= {0}) as saldo";
        return ctx.fetchSingle(sql, dataSaldo).into(BigDecimal.class);
    }

    public BigDecimal getSaldoLR8() {
        String sql = "select "
                + "(select coalesce(sum(q.importo), 0) as quietanze from quietanza q where q.id_codice in (select id from codice where c02 = 'LR8'))-"
                + "(select coalesce(sum(o.importo), 0) as ordinativi from ordinativo o where o.id_codice in (select id from codice where c02 = 'LR8'))+"
                + "(select coalesce(sum(mv.importo), 0) as virtuali from movimenti_virtuali mv where mv.id_codice in (select id from codice where c02 = 'LR8')) as lr8";
        return ctx.fetchSingle(sql).into(BigDecimal.class);
    }

    public int generaCompetenze(int annoAttuale) {
        Mutables.MutableInteger total = new Mutables.MutableInteger();
        ctx.transaction(tx -> {
            final List<CapitoloCompetenza> lCapCompAttuali = getCapitoliCompetenze(annoAttuale);
            final List<CapitoloCompetenza> lCapCompPrec = getCapitoliCompetenze(true, annoAttuale - 1);

            final List<CompetenzaRecord> lNew = new ArrayList<>();
            lCapCompPrec.forEach(ccp -> {
                ccp.setAnno(annoAttuale);
                if (!cercaCompetenza(lCapCompAttuali, ccp)) {
                    CompetenzaRecord cr = new CompetenzaRecord();
                    cr.setIdCapitolo(ccp.getIdCapitolo());
                    cr.setChiuso((byte) 0);
                    cr.setAnno(annoAttuale);

                    lNew.add(cr);
                }
            });

            int cnt = 0;
            for (CompetenzaRecord cr : lNew) {
                try {
                    cnt += tx.dsl().insertInto(Tables.COMPETENZA).set(cr).execute();
                } catch (DataAccessException dae) {
                    if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                        throw new IntegrityException(dae.toString());
                    }

                    throw dae;
                }
            }
            if (cnt != lNew.size()) {
                throw new IntegrityException("Inserimento bulk su competenza non riuscito. Da inserire " + lNew.size() + ", inseriti " + cnt);
            }

            LogOperazioniRecord lor = new LogOperazioniRecord();
            lor.setService("CompetenzaService.generaCompetenze(int)");
            lor.setArg1(String.valueOf(annoAttuale));
            lor.setArg2(lNew.toString());
            lor.setOperatore((user != null) ? user.getCurrentUser().getUsername() : "N/A");
            lor.setDataOra(LocalDateTime.now());
            try {
                cnt = tx.dsl().insertInto(Tables.LOG_OPERAZIONI).set(lor).execute();
            } catch (DataAccessException dae) {
                if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                    throw new IntegrityException(dae.toString());
                }

                throw dae;
            }

            if (cnt != 1) {
                throw new IntegrityException("Impossibile effettuare il log dell'operazione di generazione competenze anno " + annoAttuale);
            }

            total.i = lNew.size();
        });

        return total.i;
    }

    protected boolean cercaCompetenza(List<CapitoloCompetenza> list, CapitoloCompetenza cc) {
        for (CapitoloCompetenza e : list) {
            if (e.getAnno() == cc.getAnno() && e.getIdCapitolo() == cc.getIdCapitolo()) {
                return true;
            }
        }

        return false;
    }

    public List<CapitoloCompetenza> getCapitoliCompetenzeConStanziamento(Integer anno) {
        Condition cond = anno != null ? DSL.and(Tables.COMPETENZA.ANNO.between(anno - 1, anno)) : DSL.noCondition();
        return ctx.select(Tables.COMPETENZA.ID, Tables.COMPETENZA.ID_CAPITOLO, Tables.CAPITOLO.DESCRIZIONE, DSL.ifnull(Tables.CAPITOLO.STANZIAMENTO, Tables.COMPETENZA.STANZIAMENTO).as(Tables.COMPETENZA.STANZIAMENTO), Tables.CAPITOLO.NUOVOANNO, Tables.COMPETENZA.ANNO, Tables.COMPETENZA.CHIUSO)
                .from(Tables.CAPITOLO).innerJoin(Tables.COMPETENZA).on(Tables.CAPITOLO.ID.eq(Tables.COMPETENZA.ID_CAPITOLO))
                .where(Tables.CAPITOLO.STANZIAMENTO.isNotNull().or(Tables.CAPITOLO.MOSTRASITUAZIONE.eq((byte) 1))).and(cond)
                .orderBy(Tables.CAPITOLO.DESCRIZIONE, Tables.COMPETENZA.ANNO.desc()) 
                .fetch().into(CapitoloCompetenza.class);
    }

    public List<Integer> getAnnualita(boolean soloCorrenti) {
        Condition cond = DSL.noCondition();
        if (soloCorrenti) {
            cond = cond.and(Tables.COMPETENZA.ANNO.le(LocalDate.now().getYear()));
        }
        return ctx.selectDistinct(Tables.COMPETENZA.ANNO).from(Tables.COMPETENZA).where(cond).orderBy(Tables.COMPETENZA.ANNO.desc()).fetchInto(Integer.class);
    }

    public List<RichiestaRecord> getRichieste(int idCapComp) {
        return ctx.selectFrom(Tables.RICHIESTA).where(Tables.RICHIESTA.ID_COMPETENZA.eq(idCapComp)).orderBy(Tables.RICHIESTA.DATA_PROTOCOLLO.desc()).fetch();
    }

    public RichiestaRecord getRichiestaById(Integer id) {
        return ctx.selectFrom(Tables.RICHIESTA).where(Tables.RICHIESTA.ID.eq(id)).fetchOne();
    }

    @LogDatabaseOperation
    public void elimina(RichiestaRecord r) {
        ctx.transaction(tx -> {
            try {
                String fileName = documentFolder + "/" + r.getNomefileLocale();
                int cnt = tx.dsl().deleteFrom(Tables.RICHIESTA).where(Tables.RICHIESTA.ID.eq(r.getId()).and(Tables.RICHIESTA.VERSIONE.eq(r.getVersione()))).execute();

                if (cnt != 1) {
                    throw new IntegrityException("Impossibile eliminare la richiesta. Proabibimete è stata eliminata da un altro utente. Aggiornare e riprovare.");
                }

                try {
                    Files.delete(Paths.get(fileName));
                    logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", fileName);
                } catch (IOException e) {
                }
            } catch (DataAccessException dae) {
                if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                    throw new IntegrityException(dae.toString());
                }

                throw dae;
            }
        });
    }

    @LogDatabaseOperation
    public void salva(RichiestaRecord r, Documento doc) {
        String uuidName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
        String fileName = documentFolder + "/" + uuidName;

        ctx.transaction(tx -> {
            try {
                Files.write(Paths.get(fileName), doc.getContent());
                logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", uuidName, doc.getFileName());

                r.setNomefile(doc.getFileName());
                r.setNomefileLocale(uuidName);
                tx.dsl().insertInto(Tables.RICHIESTA).set(r).execute();

            } catch (DataAccessException dae) {
                try {
                    Files.delete(Paths.get(fileName));
                    logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", fileName);
                } catch (IOException e) {
                }
                
                if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                    throw new IntegrityException(dae.toString());
                }

                throw dae;
            } catch (IOException ioe) {
                throw new UploadException("Impossibile caricare il file " + doc.getFileName() + " a causa di " + ioe.getMessage());
            }
        });
    }
}
