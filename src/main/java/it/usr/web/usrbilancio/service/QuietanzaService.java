/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.MovimentiVirtualiRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
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
public class QuietanzaService {
    public final static String BLANK_DOCUMENT = "__blank.pdf";
    @Inject
    @AppLogger
    Logger logger;
    @DSLBilancio
    @Inject
    DSLContext ctx;
    @DocumentFolder
    @Inject
    String documentFolder;

    public List<QuietanzaRecord> getQuietanze() {
        return ctx.selectFrom(Tables.QUIETANZA).orderBy(Tables.QUIETANZA.DATA_PAGAMENTO.desc()).fetch();
    }

    public QuietanzaRecord getQuietanzaById(int id) {
        return ctx.selectFrom(Tables.QUIETANZA).where(Tables.QUIETANZA.ID.eq(id)).fetchOne();
    }

    public List<QuietanzaRecord> getQuietanzeCompetenza(int idCompetenza) {
        return ctx.selectFrom(Tables.QUIETANZA).where(Tables.QUIETANZA.ID_COMPETENZA.eq(idCompetenza)).fetch();
    }

    @LogDatabaseOperation
    public void inserisci(QuietanzaRecord q, Documento doc) {
        Mutables.MutableBoolean fileSaved = new Mutables.MutableBoolean();
        if(doc!=null) {
            q.setNomefileLocale(UUID.randomUUID().toString()+"."+FilenameUtils.getExtension(doc.getFileName()));
            q.setNomefile(doc.getFileName());            
        }
        String fileName = documentFolder + "/" + q.getNomefileLocale();
        
        try {
            ctx.transaction(trx -> {
                if(doc!=null) {
                    try {
                        Files.write(Paths.get(fileName), doc.getContent());
                    } catch (IOException ioe) {
                        throw new UploadException("Impossibile caricare il file " + doc.getFileName() + " a causa di " + ioe.getMessage());
                    }
                    fileSaved.flag = true;
                }
                
                trx.dsl().insertInto(Tables.QUIETANZA).set(q).execute();
            });
        } catch (DataAccessException dae) {
            if (fileSaved.flag) {
                try {
                    Files.delete(Paths.get(fileName));
                } catch (IOException e) {
                }
            }

            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    public BigDecimal getTotaleQuietanze(int idCompetenza) {
        return ctx.select(DSL.coalesce(DSL.sum(Tables.QUIETANZA.IMPORTO), BigDecimal.ZERO)).from(Tables.QUIETANZA).where(Tables.QUIETANZA.ID_COMPETENZA.eq(idCompetenza)).fetchSingle().value1();
    }
    
    @LogDatabaseOperation
    public void modifica(QuietanzaRecord q, Documento doc) {
        Mutables.MutableBoolean fileSaved = new Mutables.MutableBoolean();
        Mutables.MutableString fileName = new Mutables.MutableString();

        try {
            long oldV = q.getVersione();
            q.setVersione(oldV + 1);

            // nessun documento allegato, aggiorna solo il record
            if (doc == null) {
                int updated = ctx.update(Tables.QUIETANZA).set(q).where(Tables.QUIETANZA.ID.eq(q.getId())).and(Tables.QUIETANZA.VERSIONE.eq(oldV)).execute();
                if (updated != 1) {
                    throw new StaleRecordException("Il record è stato già aggiornato.");
                }
            } else {
                // tieni da parte il vecchio file
                String oldFileLocale = q.getNomefileLocale();

                // Genera il nome del nuovo file                 
                q.setNomefileLocale(UUID.randomUUID().toString()+"."+FilenameUtils.getExtension(doc.getFileName()));
                q.setNomefile(doc.getFileName());
                fileName.s = documentFolder + "/" + q.getNomefileLocale();

                ctx.transaction(trx -> {
                    try {
                        Files.write(Paths.get(fileName.s), doc.getContent());
                    } catch (IOException ioe) {
                        throw new UploadException("Impossibile caricare il file " + doc.getFileName() + " a causa di " + ioe.getMessage());
                    }
                    fileSaved.flag = true;

                    int updated = ctx.update(Tables.QUIETANZA).set(q).where(Tables.QUIETANZA.ID.eq(q.getId())).and(Tables.QUIETANZA.VERSIONE.eq(oldV)).execute();
                    if (updated != 1) {
                        throw new StaleRecordException("Il record è stato già aggiornato.");
                    }
                });

                try {
                    if(!BLANK_DOCUMENT.equalsIgnoreCase(oldFileLocale)) {
                        Files.delete(Paths.get(documentFolder + "/" + oldFileLocale));
                    }
                } catch (IOException ioe) {
                    logger.warn("Il record di movimenti virtuali con id [{}] è stato aggiornato ma il file [{}] non è stato cancellato a causa di {}", q.getId(), q.getNomefileLocale(), ioe);
                }
            }
        } catch (DataAccessException dae) {
            if (fileSaved.flag && fileName.s != null) {
                try {
                    Files.delete(Paths.get(fileName.s));
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
    public void elimina(QuietanzaRecord q) {
        try {
            int deleted = ctx.delete(Tables.QUIETANZA).where(Tables.QUIETANZA.ID.eq(q.getId())).and(Tables.QUIETANZA.VERSIONE.eq(q.getVersione())).execute();
            if (deleted != 1) {
                throw new StaleRecordException("Il record è stato già eliminato.");
            }

            try {
                if(!BLANK_DOCUMENT.equalsIgnoreCase(q.getNomefileLocale())) {
                    Files.delete(Paths.get(documentFolder + "/" + q.getNomefileLocale()));
                }
            } catch (IOException ioe) {
                logger.warn("Il record di quietanza con id [{}] è stato eliminato ma il file [{}] non è stato cancellato a causa di {}", q.getId(), q.getNomefileLocale(), ioe);
            }
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new IntegrityException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    public List<QuietanzaRecord> cerca(SearchCriteria sc) {
                Condition cond = DSL.noCondition();
        
        if(notEmpty(sc.testo)) {
            sc.testo = "%"+sc.testo+"%";
            cond = cond.and(Tables.QUIETANZA.ORDINANTE.like(sc.testo).or(Tables.QUIETANZA.DESCRIZIONE_ORDINANZA.like(sc.testo)).or(Tables.QUIETANZA.NOTE.like(sc.testo)));
        }
        
        if(notEmpty(sc.competenze)) {
            List<Integer> lComp = Arrays.stream(sc.competenze).map(CapitoloCompetenza::getId).collect(Collectors.toList());
            
            if(sc.competenzeAnd) {
                cond = cond.and(Tables.QUIETANZA.ID_COMPETENZA.in(lComp));
            }
            else {
                cond = cond.or(Tables.QUIETANZA.ID_COMPETENZA.in(lComp));
            }
        }
        
        if(sc.dataDocDa!=null || sc.dataDocA!=null) {
            Condition condDataDoc = DSL.noCondition();
            
            if(sc.dataDocDa!=null) {
                condDataDoc = condDataDoc.or(Tables.QUIETANZA.DATA_DOCUMENTO.ge(sc.dataDocDa));
            }

            if(sc.dataDocA!=null) {
                if(sc.dataDocDa!=null) {
                    condDataDoc = condDataDoc.and(Tables.QUIETANZA.DATA_DOCUMENTO.le(sc.dataDocA));                    
                }
                else {
                    condDataDoc = condDataDoc.or(Tables.QUIETANZA.DATA_DOCUMENTO.le(sc.dataDocA));
                }
            }
            
            if(sc.dataDocAnd) {                                
                cond = cond.and(condDataDoc);
            }
            else {
                cond = cond.or(condDataDoc);
            }                 
        }
        
        if(sc.dataPagDa!=null || sc.dataPagA!=null) {
            Condition condDataPag = DSL.noCondition();
            
            if(sc.dataPagDa!=null) {
                condDataPag = condDataPag.or(Tables.QUIETANZA.DATA_PAGAMENTO.ge(sc.dataPagDa));
            }

            if(sc.dataDocA!=null) {
                if(sc.dataPagDa!=null) {
                    condDataPag = condDataPag.and(Tables.QUIETANZA.DATA_PAGAMENTO.le(sc.dataPagA));                    
                }
                else {
                    condDataPag = condDataPag.or(Tables.QUIETANZA.DATA_PAGAMENTO.le(sc.dataPagA));
                }
            }
            
            if(sc.dataPagAnd) {                                
                cond = cond.and(condDataPag);
            }
            else {
                cond = cond.or(condDataPag);
            }                 
        }
        
        if(sc.importoDa!=null || sc.importoA!=null) {
            Condition condImp = DSL.noCondition();
            
            if(sc.importoDa!=null) {
                condImp = condImp.or(Tables.QUIETANZA.IMPORTO.ge(sc.importoDa));                
            }

            if(sc.importoA!=null) {
                if(sc.importoDa!=null) {
                    condImp = condImp.and(Tables.QUIETANZA.IMPORTO.le(sc.importoA));                
                }
                else {
                    condImp = condImp.or(Tables.QUIETANZA.IMPORTO.le(sc.importoA));                
                }                    
            }
            
            if(sc.importoAnd) {                                
                cond = cond.and(condImp);
            }
            else {
                cond = cond.or(condImp);
            }                  
        }
                       
        if(sc.tipiRts!=null && sc.tipiRts.length>0) {
            List<Integer> lTipiRts = Arrays.stream(sc.tipiRts).map(TipoRtsRecord::getId).collect(Collectors.toList());
            if(sc.tipiRtsAnd) {
                cond = cond.and(Tables.QUIETANZA.ID_TIPO_RTS.in(lTipiRts));
            }
            else {
                cond = cond.or(Tables.QUIETANZA.ID_TIPO_RTS.in(lTipiRts));
            }
        }
        
        if(sc.codici!=null && sc.codici.length>0) {
            List<Integer> lCodici = Arrays.stream(sc.codici).map(CodiceRecord::getId).collect(Collectors.toList());
            if(sc.codiciAnd) {
                cond = cond.and(Tables.QUIETANZA.ID_CODICE.in(lCodici));
            }
            else {
                cond = cond.or(Tables.QUIETANZA.ID_CODICE.in(lCodici));
            }
        }
        
        if(sc.annoCompetenza!=null) {
            if(sc.annoCompAnd) {
                cond = cond.and(Tables.COMPETENZA.ANNO.eq(sc.annoCompetenza));
            }
            else {
                cond = cond.or(Tables.COMPETENZA.ANNO.eq(sc.annoCompetenza));
            }
        }
        
        if(sc.annoCompetenza!=null) {
            return ctx.select().from(Tables.QUIETANZA).join(Tables.COMPETENZA).on(Tables.QUIETANZA.ID_COMPETENZA.eq(Tables.COMPETENZA.ID)).where(cond).fetchInto(Tables.QUIETANZA);
        }
        else {
            return ctx.selectFrom(Tables.QUIETANZA).where(cond).fetch();
        }
    }
    
    private boolean notEmpty(String s) {
        return (s==null) ? false : s.trim().length()>0;
    }
    
    private boolean notEmpty(Object[] o) {
        return (o == null) ? false : o.length > 0;
    }
    
    public BigDecimal getTotaleQuietanzePeriodo(LocalDate min, LocalDate max) {
        String sql = "SELECT sum(importo) as tot from quietanza q where (q.data_pagamento between {0} and {1})";
        return ctx.fetchSingle(sql, min, max).into(BigDecimal.class);
    }
    
    @LogDatabaseOperation
    public void trasforma(QuietanzaRecord q, MovimentiVirtualiRecord mv, Documento doc) {
        ctx.transaction(tx -> {
            String nomeFileLocale = UUID.randomUUID().toString()+"."+FilenameUtils.getExtension(doc.getFileName());
            
            try {                                
                Files.write(Paths.get(documentFolder + "/" + nomeFileLocale), doc.getContent());                
            
                q.setNomefile(doc.getFileName());
                q.setNomefileLocale(nomeFileLocale);
                int numIns = tx.dsl().insertInto(Tables.QUIETANZA).set(q).execute();
                int numDel = tx.dsl().deleteFrom(Tables.MOVIMENTI_VIRTUALI).where(Tables.MOVIMENTI_VIRTUALI.ID.eq(mv.getId()).and(Tables.MOVIMENTI_VIRTUALI.VERSIONE.eq(mv.getVersione()))).execute();

                if((numIns-numDel)!=0) {
                    throw new IntegrityException("Difformità nell'operazione di trasformazione. Inseriti: "+numIns+", eliminati: "+numDel);
                }
            
                String fileName = documentFolder + "/" + mv.getNomefileLocale();
                try {
                    Files.delete(Paths.get(fileName));
                    logger.info("Il file locale [{}] associato al movimento id [{}] rimosso dal volume.", mv.getNomefileLocale(), mv.getId());
                }
                catch(IOException ioe) {
                }                            
            } 
            catch(IntegrityException ie) {
                try {
                    Files.delete(Paths.get(documentFolder + "/" + nomeFileLocale));
                    logger.info("Il file locale [{}] associato al movimento id [{}] rimosso dal volume.", mv.getNomefileLocale(), mv.getId());
                }
                catch(IOException ioe) {
                }            
            }
            catch(IOException ioe) {
                throw new UploadException("Impossibile caricare il file " + doc.getFileName() + " a causa di " + ioe.getMessage());
            }
        });
    }
}
