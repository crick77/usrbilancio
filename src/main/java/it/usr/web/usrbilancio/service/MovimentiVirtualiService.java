/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.MovimentiVirtualiRecord;
import it.usr.web.usrbilancio.interceptor.LogDatabaseOperation;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import it.usr.web.usrbilancio.producer.DocumentFolder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.UUID;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class MovimentiVirtualiService {

    @Inject
    @AppLogger
    Logger logger;
    @DSLBilancio
    @Inject
    DSLContext ctx;
    @DocumentFolder
    @Inject
    String documentFolder;

    public List<MovimentiVirtualiRecord> getMovimentiVirtuali(int statoCap) {
        if(statoCap==-1) {
            return ctx.selectFrom(Tables.MOVIMENTI_VIRTUALI).fetch();
        }
        else {
            return ctx.select().from(Tables.MOVIMENTI_VIRTUALI.join(Tables.COMPETENZA).on(Tables.MOVIMENTI_VIRTUALI.ID_COMPETENZA.eq(Tables.COMPETENZA.ID))).where(Tables.COMPETENZA.CHIUSO.eq((byte)statoCap)).fetchInto(MovimentiVirtualiRecord.class);
        }
    }

    public List<MovimentiVirtualiRecord> getMovimentiVirtuali(int idCapComp, int statoCap) {
        if(statoCap==-1) {
            return ctx.selectFrom(Tables.MOVIMENTI_VIRTUALI).where(Tables.MOVIMENTI_VIRTUALI.ID_COMPETENZA.eq(idCapComp)).fetch();
        }
        else {
            return ctx.select().from(Tables.MOVIMENTI_VIRTUALI.join(Tables.COMPETENZA).on(Tables.MOVIMENTI_VIRTUALI.ID_COMPETENZA.eq(Tables.COMPETENZA.ID))).where(Tables.COMPETENZA.CHIUSO.eq((byte)statoCap).and(Tables.MOVIMENTI_VIRTUALI.ID_COMPETENZA.eq(idCapComp))).fetchInto(MovimentiVirtualiRecord.class);
        }
    }

    public MovimentiVirtualiRecord getMovimentoVirtualeById(int id) {
        return ctx.selectFrom(Tables.MOVIMENTI_VIRTUALI).where(Tables.MOVIMENTI_VIRTUALI.ID.eq(id)).fetchOne();
    }

    public BigDecimal getTotaleMovimenti(int idCompetenza) {
        return ctx.select(DSL.coalesce(DSL.sum(Tables.MOVIMENTI_VIRTUALI.IMPORTO), BigDecimal.ZERO)).from(Tables.MOVIMENTI_VIRTUALI).where(Tables.MOVIMENTI_VIRTUALI.ID_COMPETENZA.eq(idCompetenza)).fetchSingle().value1();
    }
    
    @LogDatabaseOperation
    public void inserisci(MovimentiVirtualiRecord mvr, Documento doc) {
        Mutables.MutableBoolean fileSaved = new Mutables.MutableBoolean();
        fileSaved.flag = false;

        String uuidName = null;
        String fileName = null;
        if (doc != null) {
            uuidName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
            fileName = documentFolder + "/" + uuidName;
        }

        try {
            if (doc != null) {
                mvr.setNomefileLocale(uuidName);
                mvr.setNomefile(doc.getFileName());

                try {
                    Files.write(Paths.get(fileName), doc.getContent());
                    logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", uuidName, doc.getFileName());

                    fileSaved.flag = true;
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file " + doc.getFileName() + " a causa di " + ioe.getMessage());
                }
            }

            ctx.insertInto(Tables.MOVIMENTI_VIRTUALI).set(mvr).execute();
        } catch (DataAccessException dae) {
            if (fileSaved.flag) {
                try {
                    Files.delete(Paths.get(fileName));
                    logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", fileName);
                } catch (IOException e) {
                }
            }

            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void modifica(MovimentiVirtualiRecord mv, Documento doc) {
        Mutables.MutableBoolean fileSaved = new Mutables.MutableBoolean();
        Mutables.MutableString fileName = new Mutables.MutableString();

        try {
            long oldV = mv.getVersione();
            mv.setVersione(oldV + 1);

            // nessun documento allegato, aggiorna solo il record
            if (doc == null) {
                int updated = ctx.update(Tables.MOVIMENTI_VIRTUALI).set(mv).where(Tables.MOVIMENTI_VIRTUALI.ID.eq(mv.getId())).and(Tables.MOVIMENTI_VIRTUALI.VERSIONE.eq(oldV)).execute();
                if (updated != 1) {
                    throw new StaleRecordException("Il record è stato già aggiornato.");
                }
            } else { 
                // tieni da parte il vecchio file
                String oldFileLocale = mv.getNomefileLocale();

                // Genera il nome del nuovo file                 
                mv.setNomefileLocale(UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName()));
                mv.setNomefile(doc.getFileName());
                fileName.s = documentFolder + "/" + mv.getNomefileLocale();

                ctx.transaction(trx -> {
                    try {
                        Files.write(Paths.get(fileName.s), doc.getContent());
                        logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", fileName.s, doc.getFileName());

                        fileSaved.flag = true;
                    } catch (IOException ioe) {
                        throw new UploadException("Impossibile caricare il file " + doc.getFileName() + " a causa di " + ioe.getMessage());
                    }

                    int updated = ctx.update(Tables.MOVIMENTI_VIRTUALI).set(mv).where(Tables.MOVIMENTI_VIRTUALI.ID.eq(mv.getId())).and(Tables.MOVIMENTI_VIRTUALI.VERSIONE.eq(oldV)).execute();
                    if (updated != 1) {
                        throw new StaleRecordException("Il record è stato già aggiornato.");
                    }
                }); 

                try {
                    if (oldFileLocale!=null && !"__blank.pdf".equalsIgnoreCase(oldFileLocale)) {
                        Files.delete(Paths.get(documentFolder + "/" + oldFileLocale));
                        logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", oldFileLocale);
                    }
                } catch (IOException ioe) {
                    logger.warn("Il record di movimenti virtuali con id [{}] è stato aggiornato ma il file [{}] non è stato cancellato a causa di {}", mv.getId(), mv.getNomefileLocale(), ioe);
                }
            }
        } catch (DataAccessException dae) {
            if (fileSaved.flag && fileName.s != null) {
                try {
                    Files.delete(Paths.get(fileName.s));
                    logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", fileName.s);
                } catch (IOException e) {
                }
            }

            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void elimina(MovimentiVirtualiRecord mv) {
        try {
            int deleted = ctx.delete(Tables.MOVIMENTI_VIRTUALI).where(Tables.MOVIMENTI_VIRTUALI.ID.eq(mv.getId())).and(Tables.MOVIMENTI_VIRTUALI.VERSIONE.eq(mv.getVersione())).execute();
            if (deleted != 1) {
                throw new StaleRecordException("Il record è stato già eliminato.");
            }

            try {
                Files.delete(Paths.get(documentFolder + "/" + mv.getNomefileLocale()));
                logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", mv.getNomefileLocale());
            } catch (IOException ioe) {
                logger.warn("Il record di movimenti virtuali con id [{}] è stato eliminato ma il file [{}] non è stato cancellato a causa di {}", mv.getId(), mv.getNomefileLocale(), ioe);
            }
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IntegrityException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    public List<MovimentiVirtualiRecord> cerca(SearchCriteria sc) {
        Condition cond = DSL.noCondition();

        if (notEmpty(sc.testo)) {
            sc.testo = "%" + sc.testo + "%";
            cond = cond.and(Tables.MOVIMENTI_VIRTUALI.DESCRIZIONE_RAGIONERIA.like(sc.testo).or(Tables.MOVIMENTI_VIRTUALI.NOTE.like(sc.testo)).or(Tables.MOVIMENTI_VIRTUALI.NOMINATIVO.like(sc.testo)));
        }

        if(notEmpty(sc.competenze)) {
            List<Integer> lComp = Arrays.stream(sc.competenze).map(CapitoloCompetenza::getId).collect(Collectors.toList());
            
            if(sc.competenzeAnd) {
                cond = cond.and(Tables.MOVIMENTI_VIRTUALI.ID_COMPETENZA.in(lComp));
            }
            else {
                cond = cond.or(Tables.MOVIMENTI_VIRTUALI.ID_COMPETENZA.in(lComp));
            }
        }
        
        if (sc.dataPagDa != null || sc.dataPagA != null) {            
            Condition condDataPag = DSL.noCondition();
            if (sc.dataPagDa != null) {                
                condDataPag = condDataPag.or(Tables.MOVIMENTI_VIRTUALI.DATA_PAGAMENTO.ge(sc.dataPagDa));
            }

            if (sc.dataPagA != null) {
                if (sc.dataPagDa != null) {                    
                    condDataPag = condDataPag.and(Tables.MOVIMENTI_VIRTUALI.DATA_PAGAMENTO.le(sc.dataPagA));
                } else {                    
                    condDataPag = condDataPag.or(Tables.MOVIMENTI_VIRTUALI.DATA_PAGAMENTO.le(sc.dataPagA));
                }
            }

            if (sc.dataPagAnd) {
                cond = cond.and(condDataPag);
            } else {
                cond = cond.or(condDataPag);
            }
        }

        if (sc.dataDocDa != null || sc.dataDocA != null) {
            Condition condDataDoc = DSL.noCondition();
            
            if (sc.dataDocDa != null) {
                condDataDoc = condDataDoc.or(Tables.MOVIMENTI_VIRTUALI.DATA_DOCUMENTO.ge(sc.dataDocDa));            
            }

            if (sc.dataDocA != null) {
                if (sc.dataDocDa != null) {
                    condDataDoc = condDataDoc.and(Tables.MOVIMENTI_VIRTUALI.DATA_DOCUMENTO.le(sc.dataDocA));                    
                } else {
                    condDataDoc = condDataDoc.or(Tables.MOVIMENTI_VIRTUALI.DATA_DOCUMENTO.le(sc.dataDocA));                    
                }
            }

            if (sc.dataDocAnd) {
                cond = cond.and(condDataDoc);
            } else {
                cond = cond.or(condDataDoc);
            }
        }
        
        if (sc.importoDa != null || sc.importoA != null) {
            Condition condImp = DSL.noCondition();

            if (sc.importoDa != null) {
                condImp = condImp.or(Tables.MOVIMENTI_VIRTUALI.IMPORTO.ge(sc.importoDa));
            }

            if (sc.importoA != null) {
                if (sc.importoDa != null) {
                    condImp = condImp.and(Tables.MOVIMENTI_VIRTUALI.IMPORTO.le(sc.importoA));
                } else {
                    condImp = condImp.or(Tables.MOVIMENTI_VIRTUALI.IMPORTO.le(sc.importoA));
                }
            }

            if (sc.importoAnd) {
                cond = cond.and(condImp);
            } else {
                cond = cond.or(condImp);
            }
        }

        if (sc.codici != null && sc.codici.length > 0) {
            List<Integer> lCodici = Arrays.stream(sc.codici).map(CodiceRecord::getId).collect(Collectors.toList());
            if (sc.codiciAnd) {
                cond = cond.and(Tables.MOVIMENTI_VIRTUALI.ID_CODICE.in(lCodici));
            } else {
                cond = cond.or(Tables.MOVIMENTI_VIRTUALI.ID_CODICE.in(lCodici));
            }
        }

        if (sc.annoCompetenza != null) {
            if (sc.annoCompAnd) {
                cond = cond.and(Tables.COMPETENZA.ANNO.eq(sc.annoCompetenza));
            } else {
                cond = cond.or(Tables.COMPETENZA.ANNO.eq(sc.annoCompetenza));
            }
        }

        if (sc.annoCompetenza != null) {
            return ctx.select().from(Tables.MOVIMENTI_VIRTUALI).join(Tables.COMPETENZA).on(Tables.MOVIMENTI_VIRTUALI.ID_COMPETENZA.eq(Tables.COMPETENZA.ID)).where(cond).fetchInto(Tables.MOVIMENTI_VIRTUALI);
        } else {
            return ctx.selectFrom(Tables.MOVIMENTI_VIRTUALI).where(cond).fetch();
        }
    }

    private boolean notEmpty(String s) {
        return (s == null) ? false : s.trim().length() > 0;
    }
    
    private boolean notEmpty(Object[] o) {
        return (o == null) ? false : o.length > 0;
    }
}
