/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoRecord;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.interceptor.LogDatabaseOperation;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import it.usr.web.usrbilancio.producer.DocumentFolder;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.awt.Color;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.WHITE;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.vandeseer.easytable.TableDrawer;
import static org.vandeseer.easytable.settings.HorizontalAlignment.*;
import static org.vandeseer.easytable.settings.VerticalAlignment.TOP;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OrdinativoService {

    private static final float POINTS_PER_INCH = 72;
    private static final float MM_PER_INCH = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private final static Color BLUE_DARK = new Color(76, 129, 190);
    private final static Color BLUE_LIGHT_1 = new Color(186, 206, 230);
    private final static Color BLUE_LIGHT_2 = new Color(218, 230, 242);
    private static final float PADDING = 50f;
    private final static PDRectangle A3_LANDSCAPE = new PDRectangle(420 * MM_PER_INCH, 297 * MM_PER_INCH);
    @DSLBilancio
    @Inject
    DSLContext ctx;
    @AppLogger
    @Inject
    Logger logger;
    @DocumentFolder
    @Inject
    String documentFolder;

    public List<OrdinativoRecord> getOrdinativi() {
        return ctx.selectFrom(Tables.ORDINATIVO).orderBy(Tables.ORDINATIVO.DATA_PAGAMENTO.desc()).fetch();
    }

    public OrdinativoRecord getOrdinativoById(int id) {
        return ctx.selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.ID.eq(id)).fetchOne();
    }

    public OrdinativoRecord getOrdinativoOrdinativoIva(int ordinativoIva) {
        return ctx.selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.ORDINATIVO_IVA.eq(ordinativoIva)).fetchOne();
    }

    public List<OrdinativoRecord> getOrdinativiByCompetenza(int idCompetenza) {
        return ctx.selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.ID_COMPETENZA.eq(idCompetenza)).orderBy(Tables.ORDINATIVO.NUMERO_DOCUMENTO.desc()).fetch();
    }

    public List<OrdinativoRecord> getOrdinativiIvaPeriodo(int idCompetenza, LocalDate from, LocalDate to, int mostra) {
        SelectConditionStep<OrdinativoRecord> q = ctx.selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.DATA_PAGAMENTO.between(from, to))
                .and(Tables.ORDINATIVO.IMPORTO_IVA.greaterThan(BigDecimal.ZERO));
        if (idCompetenza == -1) {
            q = q.and(Tables.ORDINATIVO.DATA_PAGAMENTO.between(from, to));
        }
        switch (mostra) {
            case 1 -> {
                q = q.and(Tables.ORDINATIVO.ORDINATIVO_IVA.isNotNull()).and(Tables.ORDINATIVO.FLAG.bitAnd(2).ne(2));
            }
            case 2 -> {
                q = q.and(Tables.ORDINATIVO.ORDINATIVO_IVA.isNull());
            }
        }

        return q.fetch();
    }

    public List<OrdinativoRecord> getOrdinativiRitenutaPeriodo(int idCompetenza, LocalDate from, LocalDate to, int mostra) {
        SelectConditionStep<OrdinativoRecord> q = ctx.selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.DATA_PAGAMENTO.between(from, to))
                .and(Tables.ORDINATIVO.IMPORTO_RITENUTA.greaterThan(BigDecimal.ZERO));
        if (idCompetenza == -1) {
            q = q.and(Tables.ORDINATIVO.DATA_PAGAMENTO.between(from, to));
        }
        switch (mostra) {
            case 1 -> {
                q = q.and(Tables.ORDINATIVO.ORDINATIVO_RITENUTA.isNotNull()).and(Tables.ORDINATIVO.FLAG.bitAnd(2).ne(2));
            }
            case 2 -> {
                q = q.and(Tables.ORDINATIVO.ORDINATIVO_RITENUTA.isNull());
            }
        }
 
        return q.fetch();
    }
    
    public List<OrdinativoRecord> getOrdinativiIncompleti() {
        return ctx.selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.RTS_COMPLETO.eq((byte) 0)).orderBy(Tables.ORDINATIVO.NUMERO_PAGAMENTO).fetch();
    }

    public List<OrdinativoRecord> getOrdinativiDaStampare() {
        return ctx.selectFrom(Tables.ORDINATIVO)
                .where(Tables.ORDINATIVO.ID_TIPO_DOCUMENTO.in(DSL.select(Tables.TIPO_DOCUMENTO.ID).from(Tables.TIPO_DOCUMENTO).where(Tables.TIPO_DOCUMENTO.STAMPABILE.eq((byte) 1))))
                .and(Tables.ORDINATIVO.RTS_COMPLETO.eq((byte) 1))
                .and(Tables.ORDINATIVO.RTS_STAMPATO.eq((byte) 0))
                .orderBy(Tables.ORDINATIVO.NUMERO_PAGAMENTO)
                .fetch();
    }

    public List<OrdinativoRecord> getOrdinativiDaConsolidare() {
        return ctx.selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.CONSOLIDAMENTO.eq((byte) 1)).orderBy(Tables.ORDINATIVO.NUMERO_PAGAMENTO).fetch();
    }

    public List<OrdinativoRecord> getOrdinativiPagamento() {
        LocalDate initial = LocalDate.now().withMonth(1);
        LocalDate start = initial.withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        return ctx.selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.IMPORTO_IVA.isNull()).and(Tables.ORDINATIVO.IMPORTO_RITENUTA.isNull()).and(Tables.ORDINATIVO.DATA_PAGAMENTO.between(start, end)).fetch();
    }
        
    public List<AllegatoRecord> getAllegatiOrdinativo(int idOrdinativo) {
        return ctx.selectFrom(Tables.ALLEGATO).where(Tables.ALLEGATO.ID_ORDINATIVO.eq(idOrdinativo)).fetch();
    }

    public AllegatoRecord getAllegatoById(int id) {
        return ctx.selectFrom(Tables.ALLEGATO).where(Tables.ALLEGATO.ID.eq(id)).fetchOne();
    }

    public int contaOrdinativiIva(OrdinativoRecord o) {
        return ctx.select(DSL.count()).from(Tables.ORDINATIVO).where(Tables.ORDINATIVO.ORDINATIVO_IVA.eq(o.getId())).fetchOneInto(Integer.class);
    }

    @LogDatabaseOperation
    public void inserisci(OrdinativoRecord o, final List<Documento> documenti) {
        List<AllegatoRecord> all = new ArrayList<>();

        try {
            ctx.transaction(trx -> {
                trx.dsl().insertInto(Tables.ORDINATIVO).set(o).execute();
                int idOrdinativo = trx.dsl().lastID().intValue();

                try {
                    for (Documento doc : documenti) {
                        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
                        Files.write(Paths.get(documentFolder + "/" + localFileName), doc.getContent());
                        logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", localFileName, doc.getFileName());

                        AllegatoRecord a = new AllegatoRecord(null, idOrdinativo, doc.getGruppo(), doc.getFileName(), localFileName, doc.getContentType());
                        trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();
                        all.add(a);
                    }
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }
            });
        } catch (DataAccessException dae) {
            try {
                for (AllegatoRecord a : all) {
                    if (a.getNomefileLocale() != null) {
                        Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
                        logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", a.getNomefileLocale());
                    }
                }
            } catch (IOException e) {
            }

            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void inserisciIVA(OrdinativoRecord oIva, final List<OrdinativoRecord> ordinativi, final List<Documento> documenti) {
        List<AllegatoRecord> all = new ArrayList<>();

        try {
            ctx.transaction(trx -> {
                trx.dsl().insertInto(Tables.ORDINATIVO).set(oIva).execute();
                int idOrdinativo = trx.dsl().lastID().intValue();

                try {
                    for (Documento doc : documenti) {
                        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
                        Files.write(Paths.get(documentFolder + "/" + localFileName), doc.getContent());
                        logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", localFileName, doc.getFileName());

                        AllegatoRecord a = new AllegatoRecord(null, idOrdinativo, null, doc.getFileName(), localFileName, "F24 IVA");
                        trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();
                        all.add(a);
                    }

                    ordinativi.forEach(o -> {
                        o.setOrdinativoIva(idOrdinativo);
                        long oldV = o.getVersione();
                        o.setVersione(oldV + 1);

                        int mod = trx.dsl().update(Tables.ORDINATIVO).set(o).where(Tables.ORDINATIVO.ID.eq(o.getId())).and(Tables.ORDINATIVO.VERSIONE.eq(oldV)).execute();
                        if (mod != 1) {
                            throw new StaleRecordException("L'ordinativo [" + o.getId() + "] versione [" + o.getVersione() + "] è stato eliminato o modificato da un altro utente.");
                        }
                    });
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }
            });
        } catch (DataAccessException dae) {
            try {
                for (AllegatoRecord a : all) {
                    if (a.getNomefileLocale() != null) {
                        Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
                        logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", a.getNomefileLocale());
                    }
                }
            } catch (IOException e) {
            }

            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void inserisciIVA(Map<OrdinativoRecord, OrdinativoRecord> ordinativiIva, final Documento documento, String mese, int anno) {
        final String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(documento.getFileName());

        try {
            ctx.transaction(trx -> {
                try {
                    Files.write(Paths.get(documentFolder + "/" + localFileName), documento.getContent());
                    logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", localFileName, documento.getFileName());

                    ordinativiIva.forEach((o, oIva) -> {
                        trx.dsl().insertInto(Tables.ORDINATIVO).set(oIva).execute();
                        int idOrdinativo = trx.dsl().lastID().intValue();
                        AllegatoRecord a = new AllegatoRecord(null, idOrdinativo, documento.getGruppo(), documento.getFileName(), localFileName, "F24 IVA");
                        trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();

                        o.setOrdinativoIva(idOrdinativo);
                        long oldV = o.getVersione();
                        o.setVersione(oldV + 1);

                        int mod = trx.dsl().update(Tables.ORDINATIVO).set(o).where(Tables.ORDINATIVO.ID.eq(o.getId())).and(Tables.ORDINATIVO.VERSIONE.eq(oldV)).execute();
                        if (mod != 1) {
                            throw new StaleRecordException("L'ordinativo [" + o.getId() + "] versione [" + o.getVersione() + "] è stato eliminato o modificato da un altro utente.");
                        }
                    });

                    String pdfRiep = generaDocumentoRiepilogativoIVA(mese, anno, ordinativiIva);
                    OrdinativoRecord oRiep = new ArrayList<>(ordinativiIva.values()).get(0);
                    oRiep.setIdTipoDocumento(null);
                    oRiep.setNumeroDocumento(null);
                    oRiep.setDataDocumento(null);
                    oRiep.setBeneficiario("ERARIO");
                    oRiep.setDescrizioneRts("RIEPILOGO VERSAMENTO IVA " + anno + "-" + String.format("%02d", anno));
                    oRiep.setFatturaNumero(null);
                    oRiep.setFatturaData(null);
                    oRiep.setImporto(BigDecimal.ZERO);
                    oRiep.setImportoIva(null);
                    oRiep.setOrdinativoIva(null);
                    oRiep.setVersione(1L);
                    trx.dsl().insertInto(Tables.ORDINATIVO).set(oRiep).execute();
                    int idOrdinativo = trx.dsl().lastID().intValue();
                    AllegatoRecord a = new AllegatoRecord(null, idOrdinativo, documento.getGruppo(), "RiepilogoIVA_" + anno + "_" + String.format("%02d", anno) + ".pdf", pdfRiep, "Prospetto Riepilogo IVA");
                    trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();
                    a = new AllegatoRecord(null, idOrdinativo, documento.getGruppo(), documento.getFileName(), localFileName, "Ordinativo IVA");
                    trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();

                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }
            });
        } catch (DataAccessException dae) {
            try {
                if (localFileName != null) {
                    Files.delete(Paths.get(documentFolder + "/" + localFileName));
                    logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", localFileName);
                }
            } catch (IOException e) {
            }

            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void inserisciRitenuta(OrdinativoRecord oRitenuta, final List<OrdinativoRecord> ordinativi, final List<Documento> documenti) {
        List<AllegatoRecord> all = new ArrayList<>();

        try {
            ctx.transaction(trx -> {
                trx.dsl().insertInto(Tables.ORDINATIVO).set(oRitenuta).execute();
                int idOrdinativo = trx.dsl().lastID().intValue();

                try {
                    for (Documento doc : documenti) {
                        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
                        Files.write(Paths.get(documentFolder + "/" + localFileName), doc.getContent());
                        logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", localFileName, doc.getFileName());

                        AllegatoRecord a = new AllegatoRecord(null, idOrdinativo, null, doc.getFileName(), localFileName, doc.getContentType());
                        trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();
                        all.add(a);
                    }

                    ordinativi.forEach(o -> {
                        o.setOrdinativoRitenuta(idOrdinativo);
                        long oldV = o.getVersione();
                        o.setVersione(oldV + 1);

                        int mod = trx.dsl().update(Tables.ORDINATIVO).set(o).where(Tables.ORDINATIVO.ID.eq(o.getId())).and(Tables.ORDINATIVO.VERSIONE.eq(oldV)).execute();
                        if (mod != 1) {
                            throw new StaleRecordException("L'ordinativo [" + o.getId() + "] versione [" + o.getVersione() + "] è stato eliminato o modificato da un altro utente.");
                        }
                    });
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }
            });
        } catch (DataAccessException dae) {
            try {
                for (AllegatoRecord a : all) {
                    if (a.getNomefileLocale() != null) {
                        Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
                        logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", a.getNomefileLocale());
                    }
                }
            } catch (IOException e) {
            }

            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void inserisciRitenuta(Map<OrdinativoRecord, OrdinativoRecord> ordinativiRitenuta, final Documento documento, String mese, int anno) {
        final String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(documento.getFileName());

        try {
            ctx.transaction(trx -> {
                try {
                    Files.write(Paths.get(documentFolder + "/" + localFileName), documento.getContent());
                    logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", localFileName, documento.getFileName());

                    ordinativiRitenuta.forEach((o, oRit) -> {
                        trx.dsl().insertInto(Tables.ORDINATIVO).set(oRit).execute();
                        int idOrdinativo = trx.dsl().lastID().intValue();
                        AllegatoRecord a = new AllegatoRecord(null, idOrdinativo, documento.getGruppo(), documento.getFileName(), localFileName, "F24 Ritenuta");
                        trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();

                        o.setOrdinativoRitenuta(idOrdinativo);
                        long oldV = o.getVersione();
                        o.setVersione(oldV + 1);

                        int mod = trx.dsl().update(Tables.ORDINATIVO).set(o).where(Tables.ORDINATIVO.ID.eq(o.getId())).and(Tables.ORDINATIVO.VERSIONE.eq(oldV)).execute();
                        if (mod != 1) {
                            throw new StaleRecordException("L'ordinativo [" + o.getId() + "] versione [" + o.getVersione() + "] è stato eliminato o modificato da un altro utente.");
                        }
                    });

                    /*
                    String pdfRiep = generaDocumentoRiepilogativoIVA(mese, anno, ordinativiRitenuta);
                    OrdinativoRecord oRiep = new ArrayList<>(ordinativiRitenuta.values()).get(0);
                    oRiep.setIdTipoDocumento(null);
                    oRiep.setNumeroDocumento(null);
                    oRiep.setDataDocumento(null);
                    oRiep.setBeneficiario("ERARIO");
                    oRiep.setDescrizioneRts("RIEPILOGO VERSAMENTO IVA " + mese + " " + anno);
                    oRiep.setFatturaNumero(null);
                    oRiep.setFatturaData(null);
                    oRiep.setImporto(BigDecimal.ZERO);
                    oRiep.setImportoIva(null);
                    oRiep.setOrdinativoIva(null);
                    oRiep.setVersione(1L);
                    trx.dsl().insertInto(Tables.ORDINATIVO).set(oRiep).execute();
                    int idOrdinativo = trx.dsl().lastID().intValue();
                    AllegatoRecord a = new AllegatoRecord(null, idOrdinativo, documento.getGruppo(), "RiepilogoIVA_" + mese + "_" + anno + ".pdf", pdfRiep, "Prospetto Riepilogo IVA");
                    trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();
                    a = new AllegatoRecord(null, idOrdinativo, documento.getGruppo(), documento.getFileName(), localFileName, "Ordinativo IVA");
                    trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();
                    */

                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }
            });
        } catch (DataAccessException dae) {
            try {
                if (localFileName != null) {
                    Files.delete(Paths.get(documentFolder + "/" + localFileName));
                    logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", localFileName);
                }
            } catch (IOException e) {
            }

            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void modifica(OrdinativoRecord o) {
        long oldV = o.getVersione();
        o.setVersione(oldV + 1);
        int mod = ctx.update(Tables.ORDINATIVO).set(o).where(Tables.ORDINATIVO.ID.eq(o.getId())).and(Tables.ORDINATIVO.VERSIONE.eq(oldV)).execute();
        if (mod != 1) {
            throw new StaleRecordException("L'ordinativo [" + o.getId() + "] versione [" + o.getVersione() + "] è stato già eliminato o è stato modificato da un altro utente.");
        }
    }

    @LogDatabaseOperation
    public void modifica(OrdinativoRecord o, List<Documento> documenti) {
        List<AllegatoRecord> filesDone = new ArrayList<>();
        
        try {
            ctx.transaction(tx -> {
                for(Documento documento : documenti) {
                    String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(documento.getFileName());
                    Files.write(Paths.get(documentFolder + "/" + localFileName), documento.getContent());
                    logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", localFileName, documento.getFileName());
                    AllegatoRecord a = new AllegatoRecord(null, o.getId(), documento.getGruppo(), documento.getFileName(), localFileName, "Documento di consolidamento");
                    filesDone.add(a);
                }
                
                long oldV = o.getVersione();
                o.setVersione(oldV + 1);
                int mod = tx.dsl().update(Tables.ORDINATIVO).set(o).where(Tables.ORDINATIVO.ID.eq(o.getId())).and(Tables.ORDINATIVO.VERSIONE.eq(oldV)).execute();
                if (mod != 1) {
                    throw new StaleRecordException("L'ordinativo [" + o.getId() + "] versione [" + o.getVersione() + "] è stato già eliminato o è stato modificato da un altro utente.");
                }
                
                for(AllegatoRecord a : filesDone) {
                    tx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();
                }
            });
        }
        catch (DataAccessException dae) {
            try {
                for(AllegatoRecord all : filesDone) {
                    Files.delete(Paths.get(documentFolder + "/" + all.getNomefileLocale()));
                    logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", all.getNomefileLocale());
                }
            } catch (IOException e) {
            }

            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }
    
    @LogDatabaseOperation
    public void modifica(List<OrdinativoRecord> oList) {
        ctx.transaction(trx -> {
            oList.forEach(o -> {
                long oldV = o.getVersione();
                o.setVersione(oldV + 1);
                int mod = trx.dsl().update(Tables.ORDINATIVO).set(o).where(Tables.ORDINATIVO.ID.eq(o.getId())).and(Tables.ORDINATIVO.VERSIONE.eq(oldV)).execute();
                if (mod != 1) {
                    throw new StaleRecordException("L'ordinativo [" + o.getId() + "] versione [" + o.getVersione() + "] è stato già eliminato o è stato modificato da un altro utente.");
                }
            });
        });
    }

    @LogDatabaseOperation
    public void elimina(OrdinativoRecord o) {
        ctx.transaction(trx -> {
            List<AllegatoRecord> lAll = trx.dsl().selectFrom(Tables.ALLEGATO).where(Tables.ALLEGATO.ID_ORDINATIVO.eq(o.getId())).fetch();
            int rem = 0;
            for (AllegatoRecord a : lAll) {
                rem += trx.dsl().delete(Tables.ALLEGATO).where(Tables.ALLEGATO.ID.eq(a.getId())).execute();
            }

            if (rem == lAll.size()) {
                for (AllegatoRecord a : lAll) {
                    try {
                        Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
                        logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", a.getNomefileLocale());
                    } catch (IOException e) {
                        logger.warn("Errore nell'eliminazione del file per l'allegato id [{}] nome file [{}]. Il record è stato eliminato. Errore: {}", a.getId(), a.getNomefileLocale(), e);
                    }
                }
            } else {
                throw new StaleRecordException("Errore di eliminazione di uno o più allegati per l'ordinativo [" + o.getId() + "] versione [" + o.getVersione() + "]. Eliminati " + rem + " su " + lAll.size() + ".");
            }

            rem = trx.dsl().update(Tables.ORDINATIVO).set(Tables.ORDINATIVO.ORDINATIVO_IVA, (Integer) null).where(Tables.ORDINATIVO.ORDINATIVO_IVA.eq(o.getId())).execute();
            logger.info("Eliminazione ordinarivo con ID = [{}] - ordinativi IVA aggiornati come riferimento [{}].", o.getId(), rem);
            rem = trx.dsl().delete(Tables.ORDINATIVO).where(Tables.ORDINATIVO.ID.eq(o.getId())).and(Tables.ORDINATIVO.VERSIONE.eq(o.getVersione())).execute();
            if (rem != 1) {
                throw new StaleRecordException("L'ordinativo [" + o.getId() + "] versione [" + o.getVersione() + "] è stato già eliminato o è stato modificato da un altro utente.");
            }
        });

    }

    @LogDatabaseOperation
    public void inserisci(List<Documento> documenti, int idOrdinativo) {
        List<AllegatoRecord> all = new ArrayList<>();

        try {
            ctx.transaction(trx -> {
                try {
                    for (Documento doc : documenti) {
                        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
                        Files.write(Paths.get(documentFolder + "/" + localFileName), doc.getContent());
                        logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", localFileName, doc.getFileName());

                        AllegatoRecord a = new AllegatoRecord(null, idOrdinativo, doc.getGruppo(), doc.getFileName(), localFileName, doc.getContentType());                        
                        trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();
                        all.add(a);
                    }
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }
            });
        } catch (DataAccessException dae) {
            for (AllegatoRecord a : all) {
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
    public void modifica(AllegatoRecord a) {
        ctx.update(Tables.ALLEGATO).set(a).where(Tables.ALLEGATO.ID.eq(a.getId())).execute();
    }

    @LogDatabaseOperation
    public void elimina(AllegatoRecord a) {
        ctx.transaction(trx -> {
            int rem = trx.dsl().deleteFrom(Tables.ALLEGATO).where(Tables.ALLEGATO.ID.eq(a.getId())).execute();
            if (rem != 1) {
                throw new StaleRecordException("L'allegato [" + a.getId() + "] è stato già eliminato.");
            }

            try {
                Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
                logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", a.getNomefileLocale());
            } catch (IOException e) {
                logger.warn("Errore nell'eliminazione del file per l'allegato id [{}] nomefile [{}]. Il record è stato eliminato. Errore: {}", a.getId(), a.getNomefileLocale(), e);
            }
        });
    }

    @LogDatabaseOperation
    public void elimina(List<AllegatoRecord> allegatiSelezionati) {
        ctx.transaction(trx -> {
            // elimina i record dal db...
            allegatiSelezionati.forEach(a -> {
                int rem = trx.dsl().deleteFrom(Tables.ALLEGATO).where(Tables.ALLEGATO.ID.eq(a.getId())).execute();
                if (rem != 1) {
                    throw new StaleRecordException("L'allegato [" + a.getId() + "] è stato già eliminato.");
                }
            });

            // se tutti i record sono stati rimossi, tenta di eliminare i files...
            allegatiSelezionati.forEach(a -> {
                try {
                    Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
                    logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", a.getNomefileLocale());
                } catch (IOException e) {
                    logger.warn("Errore nell'eliminazione del file per l'allegato id [{}] nomefile [{}]. Il record è stato eliminato. Errore: {}", a.getId(), a.getNomefileLocale(), e);
                }
            }); 
        });
    } 
    
    @LogDatabaseOperation
    public void trasferisci(OrdinativoAppoggioRecord oar, boolean completato) {
        try {
            ctx.transaction(trx -> {
                // Estrae allegati di appoggio
                List<AllegatoAppoggioRecord> appAll = trx.dsl().selectFrom(Tables.ALLEGATO_APPOGGIO).where(Tables.ALLEGATO_APPOGGIO.ID_ORDINATIVO_APPOGGIO.eq(oar.getId())).fetch();

                // inserisce ordinativo
                OrdinativoRecord o = new OrdinativoRecord();
                o.setIdCompetenza(oar.getIdCompetenza());
                o.setIdTipoDocumento(oar.getIdTipoDocumento());
                o.setIdTipoRts(oar.getIdTipoRts());
                o.setIdCodice(oar.getIdCodice());
                o.setNumeroPagamento(oar.getNumeroPagamento());
                o.setDataPagamento(oar.getDataPagamento());
                o.setNumeroDocumento(oar.getNumeroDocumento());
                o.setDataDocumento(oar.getDataDocumento());
                o.setBeneficiario(oar.getBeneficiario());
                o.setDescrizioneRts(oar.getDescrizioneRts());
                o.setImporto(oar.getImporto());
                o.setNote(oar.getNote());
                o.setNoterag(oar.getNoterag());
                o.setDataRicevimento(oar.getDataRicevimento());
                o.setRtsCompleto((byte) (completato ? 1 : 0));
                o.setRtsStampato((byte) 0);
                o.setFatturaNumero(oar.getFatturaNumero());
                o.setFatturaData(oar.getFatturaData());
                o.setImportoIva(oar.getImportoIva());
                o.setImportoRitenuta(oar.getImportoRitenuta());
                o.setFlag(oar.getFlag());
                o.setVersione(1L);

                // inserisce nuovo ordinativo e preleva ID
                trx.dsl().insertInto(Tables.ORDINATIVO).set(o).execute();
                int idOrdinativo = trx.dsl().lastID().intValue();

                // inserisce gli allegati correlati
                for (AllegatoAppoggioRecord all : appAll) {
                    AllegatoRecord aa = new AllegatoRecord();
                    aa.setIdOrdinativo(idOrdinativo);
                    aa.setNomefile(all.getNomefile());
                    aa.setNomefileLocale(all.getNomefileLocale());
                    aa.setDescrizione(all.getDescrizione());

                    trx.dsl().insertInto(Tables.ALLEGATO).set(aa).execute();
                }

                // elimina gli allegati di appoggio
                int num = trx.dsl().delete(Tables.ALLEGATO_APPOGGIO).where(Tables.ALLEGATO_APPOGGIO.ID_ORDINATIVO_APPOGGIO.eq(oar.getId())).execute();
                if (num != appAll.size()) {
                    throw new StaleRecordException("Numero di allegati di appoggio rimossi diverso da quelli elaborati [" + num + "!=" + appAll.size() + "].");
                }
                // elimina l'ordinativo di appoggio
                num = trx.dsl().delete(Tables.ORDINATIVO_APPOGGIO).where(Tables.ORDINATIVO_APPOGGIO.ID.eq(oar.getId())).execute();
                if (num != 1) {
                    throw new StaleRecordException("Ordinativo di appoggio [" + oar.getId() + "] già rimosso.");
                }
            });
        } catch (StaleRecordException sre) {
            throw sre;
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void trasferisciConsolidato(OrdinativoAppoggioRecord oar) {
        try {
            ctx.transaction(trx -> {
                // Estrae allegati di appoggio
                List<AllegatoAppoggioRecord> appAll = trx.dsl().selectFrom(Tables.ALLEGATO_APPOGGIO).where(Tables.ALLEGATO_APPOGGIO.ID_ORDINATIVO_APPOGGIO.eq(oar.getId())).fetch();

                // inserisce ordinativo
                OrdinativoRecord o = new OrdinativoRecord();
                o.setIdCompetenza(oar.getIdCompetenza());
                o.setIdTipoDocumento(oar.getIdTipoDocumento());
                o.setIdTipoRts(oar.getIdTipoRts());
                o.setIdCodice(oar.getIdCodice());
                o.setNumeroPagamento(oar.getNumeroPagamento());
                o.setDataPagamento(oar.getDataPagamento());
                o.setNumeroDocumento(oar.getNumeroDocumento());
                o.setDataDocumento(oar.getDataDocumento());
                o.setBeneficiario(oar.getBeneficiario());
                o.setDescrizioneRts(oar.getDescrizioneRts());
                o.setImporto(oar.getImporto());
                o.setImportoCons(oar.getImporto());
                o.setNote(oar.getNote());
                o.setNoterag(oar.getNoterag());
                o.setDataRicevimento(oar.getDataRicevimento());
                o.setRtsCompleto((byte) 1);
                o.setRtsStampato((byte) 0);
                o.setConsolidamento((byte) 1);
                o.setFatturaNumero(oar.getFatturaNumero());
                o.setFatturaData(oar.getFatturaData());
                o.setImportoIva(oar.getImportoIva());
                o.setImportoRitenuta(oar.getImportoRitenuta());
                o.setFlag(oar.getFlag());
                o.setVersione(1L);

                // inserisce nuovo ordinativo e preleva ID
                trx.dsl().insertInto(Tables.ORDINATIVO).set(o).execute();
                int idOrdinativo = trx.dsl().lastID().intValue();

                // inserisce gli allegati correlati
                for (AllegatoAppoggioRecord all : appAll) {
                    AllegatoRecord aa = new AllegatoRecord();
                    aa.setIdOrdinativo(idOrdinativo);
                    aa.setNomefile(all.getNomefile());
                    aa.setNomefileLocale(all.getNomefileLocale());
                    aa.setDescrizione(all.getDescrizione());

                    trx.dsl().insertInto(Tables.ALLEGATO).set(aa).execute();
                }

                // elimina gli allegati di appoggio
                int num = trx.dsl().delete(Tables.ALLEGATO_APPOGGIO).where(Tables.ALLEGATO_APPOGGIO.ID_ORDINATIVO_APPOGGIO.eq(oar.getId())).execute();
                if (num != appAll.size()) {
                    throw new StaleRecordException("Numero di allegati di appoggio rimossi diverso da quelli elaborati [" + num + "!=" + appAll.size() + "].");
                }
                // elimina l'ordinativo di appoggio
                num = trx.dsl().delete(Tables.ORDINATIVO_APPOGGIO).where(Tables.ORDINATIVO_APPOGGIO.ID.eq(oar.getId())).execute();
                if (num != 1) {
                    throw new StaleRecordException("Ordinativo di appoggio [" + oar.getId() + "] già rimosso.");
                }
            });
        } catch (StaleRecordException sre) {
            throw sre;
        } catch (DataAccessException dae) {
            if (dae.getCause() instanceof SQLIntegrityConstraintViolationException) {
                throw new DuplicationException(dae.getCause().getMessage());
            }

            throw dae;
        }
    }

    public int getNumeroOrdinativiPeriodo(LocalDate from, LocalDate to) {
        return ctx.select(DSL.countDistinct(Tables.ORDINATIVO.NUMERO_PAGAMENTO)).from(Tables.ORDINATIVO).where(Tables.ORDINATIVO.DATA_PAGAMENTO.between(from, to)).fetchSingle().into(Integer.class);
    }

    public Integer getUltimoNumeroOrdinativo(int anno) {
        LocalDate from = LocalDate.now().withMonth(1).withDayOfMonth(1).withYear(anno);
        String v = ctx.select(DSL.max(Tables.ORDINATIVO.NUMERO_PAGAMENTO.cast(Long.class))).from(Tables.ORDINATIVO).where(Tables.ORDINATIVO.DATA_PAGAMENTO.ge(from)).fetchSingle().into(String.class);
        try {
            return Integer.valueOf(v);
        } catch (NumberFormatException nfe) { 
            logger.error("Impossibile calcolare l'ultimo numero di ordinativo per l'anno {}. Valore restituito: [{}]", anno, v);
            return null;
        }
    }

    public BigDecimal getImportoIVADaPagare(int annoAttuale) {
        LocalDate from = LocalDate.now().withMonth(1).withDayOfMonth(1).withYear(annoAttuale);
        LocalDate to = from.plusYears(1).minusDays(1);
        return ctx.select(DSL.sum(Tables.ORDINATIVO.IMPORTO_IVA)).from(Tables.ORDINATIVO).where(Tables.ORDINATIVO.DATA_PAGAMENTO.between(from, to)).and(Tables.ORDINATIVO.ORDINATIVO_IVA.isNull()).fetchSingle().into(BigDecimal.class);
    }

    public BigDecimal getImportoIVAPagata(int annoAttuale) {
        BigDecimal rettifica = ctx.select(Tables.RETTIFICA_IVA.IVA_PAGATA).from(Tables.RETTIFICA_IVA).where(Tables.RETTIFICA_IVA.ANNO.eq(annoAttuale)).fetchOneInto(BigDecimal.class);
        LocalDate from = LocalDate.now().withMonth(1).withDayOfMonth(1).withYear(annoAttuale);
        LocalDate to = from.plusYears(1).withMonth(1).withDayOfYear(1).minusDays(1);
        BigDecimal res = ctx.select(DSL.coalesce(DSL.sum(Tables.ORDINATIVO.IMPORTO), 0)).from(Tables.ORDINATIVO).where(Tables.ORDINATIVO.DATA_PAGAMENTO.between(from, to))
                .and(Tables.ORDINATIVO.ID.in(
                        DSL.select(Tables.ORDINATIVO.ORDINATIVO_IVA).from(Tables.ORDINATIVO).where(Tables.ORDINATIVO.DATA_PAGAMENTO.between(from, to)).and(Tables.ORDINATIVO.ORDINATIVO_IVA.isNotNull()))
                ).fetchSingle().into(BigDecimal.class);
        BigDecimal tot = rettifica != null ? res.add(rettifica) : res;
        logger.info("Valore IVA Da Pagare prima della rettifica [{}], rettifica [{}] totale [{}].", res, rettifica, tot);
        return tot;
    }

    public BigDecimal getImportoIVAAnagrafica(int annoAttuale) {
        BigDecimal rettifica = ctx.select(Tables.RETTIFICA_IVA.IVA_ANAGRAFICA).from(Tables.RETTIFICA_IVA).where(Tables.RETTIFICA_IVA.ANNO.eq(annoAttuale)).fetchOneInto(BigDecimal.class);
        LocalDate from = LocalDate.now().withMonth(1).withDayOfMonth(1).withYear(annoAttuale);
        LocalDate to = from.plusYears(1).withMonth(1).withDayOfYear(1).minusDays(1);
        BigDecimal res = ctx.select(DSL.coalesce(DSL.sum(Tables.ORDINATIVO.IMPORTO_IVA), 0)).from(Tables.ORDINATIVO).where(Tables.ORDINATIVO.DATA_PAGAMENTO.between(from, to)).fetchSingle().into(BigDecimal.class);
        BigDecimal tot = rettifica != null ? res.add(rettifica) : res;
        logger.info("Valore IVA Anagrafica prima della rettifica [{}], rettifica [{}] totale [{}].", res, rettifica, tot);
        return tot;
    }

    public List<OrdinativoRecord> cerca(SearchCriteria sc) {
        Condition cond = DSL.noCondition();

        if (notEmpty(sc.testo)) {
            sc.testo = "%" + sc.testo + "%";
            cond = cond.and(Tables.ORDINATIVO.BENEFICIARIO.like(sc.testo).or(Tables.ORDINATIVO.DESCRIZIONE_RTS.like(sc.testo)).or(Tables.ORDINATIVO.NOTE.like(sc.testo)));
        }

        if (notEmpty(sc.competenze)) {
            List<Integer> lComp = Arrays.stream(sc.competenze).map(CapitoloCompetenza::getId).collect(Collectors.toList());

            if (sc.competenzeAnd) {
                cond = cond.and(Tables.ORDINATIVO.ID_COMPETENZA.in(lComp));
            } else {
                cond = cond.or(Tables.ORDINATIVO.ID_COMPETENZA.in(lComp));
            }
        }

        if (sc.dataDocDa != null || sc.dataDocA != null) {
            Condition condDataDoc = DSL.noCondition();
            if (sc.dataDocDa != null) {
                condDataDoc = condDataDoc.or(Tables.ORDINATIVO.DATA_DOCUMENTO.ge(sc.dataDocDa));
            }

            if (sc.dataDocA != null) {
                if (sc.dataDocDa != null) {
                    condDataDoc = condDataDoc.and(Tables.ORDINATIVO.DATA_DOCUMENTO.le(sc.dataDocA));                    
                } else {
                    condDataDoc = condDataDoc.or(Tables.ORDINATIVO.DATA_DOCUMENTO.le(sc.dataDocA));
                }
            }

            if (sc.dataDocAnd) {
                cond = cond.and(condDataDoc);
            } else {
                cond = cond.or(condDataDoc);
            }
        }

        if (sc.dataPagDa != null || sc.dataPagA != null) {            
            Condition condDataPag = DSL.noCondition();
            if (sc.dataPagDa != null) {
                condDataPag = condDataPag.or(Tables.ORDINATIVO.DATA_PAGAMENTO.ge(sc.dataPagDa));
            }

            if (sc.dataPagA != null) {
                if (sc.dataPagDa != null) {                    
                    condDataPag = condDataPag.and(Tables.ORDINATIVO.DATA_PAGAMENTO.le(sc.dataPagA));
                } else {                    
                    condDataPag = condDataPag.or(Tables.ORDINATIVO.DATA_PAGAMENTO.le(sc.dataPagA));
                }
            }

            if (sc.dataPagAnd) {
                cond = cond.and(condDataPag);
            } else {
                cond = cond.or(condDataPag);
            }
        }
        
        if (sc.importoDa != null || sc.importoA != null) {
            Condition condImp = DSL.noCondition();

            if (sc.importoDa != null) {
                condImp = condImp.or(Tables.ORDINATIVO.IMPORTO.ge(sc.importoDa));
            }

            if (sc.importoA != null) {
                if (sc.importoDa != null) {
                    condImp = condImp.and(Tables.ORDINATIVO.IMPORTO.le(sc.importoA));
                } else {
                    condImp = condImp.or(Tables.ORDINATIVO.IMPORTO.le(sc.importoA));
                }
            }

            if (sc.importoAnd) {
                cond = cond.and(condImp);
            } else {
                cond = cond.or(condImp);
            }
        }

        if (sc.tipiRts != null && sc.tipiRts.length > 0) {
            List<Integer> lTipiRts = Arrays.stream(sc.tipiRts).map(TipoRtsRecord::getId).collect(Collectors.toList());
            if (sc.tipiRtsAnd) {
                cond = cond.and(Tables.ORDINATIVO.ID_TIPO_RTS.in(lTipiRts));
            } else {
                cond = cond.or(Tables.ORDINATIVO.ID_TIPO_RTS.in(lTipiRts));
            }
        }

        if (sc.codici != null && sc.codici.length > 0) {
            List<Integer> lCodici = Arrays.stream(sc.codici).map(CodiceRecord::getId).collect(Collectors.toList());
            if (sc.codiciAnd) {
                cond = cond.and(Tables.ORDINATIVO.ID_CODICE.in(lCodici));
            } else {
                cond = cond.or(Tables.ORDINATIVO.ID_CODICE.in(lCodici));
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
            return ctx.select().from(Tables.ORDINATIVO).join(Tables.COMPETENZA).on(Tables.ORDINATIVO.ID_COMPETENZA.eq(Tables.COMPETENZA.ID)).where(cond).fetchInto(Tables.ORDINATIVO);
        } else {
            return ctx.selectFrom(Tables.ORDINATIVO).where(cond).fetch();
        }
    }

    public BigDecimal getTotaleOrdinativi(int idCompetenza) {
        return ctx.select(DSL.coalesce(DSL.sum(Tables.ORDINATIVO.IMPORTO), BigDecimal.ZERO)).from(Tables.ORDINATIVO).where(Tables.ORDINATIVO.ID_COMPETENZA.eq(idCompetenza)).fetchSingle().value1();
    }

    private boolean notEmpty(String s) {
        return (s == null) ? false : s.trim().length() > 0;
    }

    private boolean notEmpty(Object[] o) {
        return (o == null) ? false : o.length > 0;
    }

    private String generaDocumentoRiepilogativoIVA(String mese, int anno, Map<OrdinativoRecord, OrdinativoRecord> ordinaitiviIva) throws IOException {
        final Table.TableBuilder tableBuilder = Table.builder()
                .addColumnsOfWidth(50, 50, 400, 120, 50, 60, 50, 60, 50, 80, 80)
                .fontSize(8)
                .font(new PDType1Font(HELVETICA))
                .borderColor(Color.WHITE);

        tableBuilder.addRow(Row.builder()
                .add(TextCell.builder().text("DEC. N.").horizontalAlignment(CENTER).borderWidth(1).build())
                .add(TextCell.builder().text("DEL").borderWidth(1).build())
                .add(TextCell.builder().text("BENEFICIARIO").borderWidth(1).build())
                .add(TextCell.builder().text("FATTURA N.").borderWidth(1).build())
                .add(TextCell.builder().text("DEL").borderWidth(1).build())
                .add(TextCell.builder().text("ORD. IMP.").borderWidth(1).build())
                .add(TextCell.builder().text("DEL").borderWidth(1).build())
                .add(TextCell.builder().text("ORD. IVA").borderWidth(1).build())
                .add(TextCell.builder().text("DEL").borderWidth(1).build())
                .add(TextCell.builder().text("IMPONIBILE").borderWidth(1).build())
                .add(TextCell.builder().text("IVA").borderWidth(1).build())
                .backgroundColor(BLUE_DARK)
                .textColor(Color.WHITE)
                .font(new PDType1Font(HELVETICA_BOLD))
                .fontSize(9)
                .horizontalAlignment(CENTER)
                .build());

        var totaleImp = new Object() {
            BigDecimal v = BigDecimal.ZERO;
        };
        var totaleIva = new Object() {
            BigDecimal v = BigDecimal.ZERO;
        };
        var i = new Object() {
            int v = 0;
        };
        ordinaitiviIva.forEach((o, oIva) -> {
            totaleImp.v = totaleImp.v.add(o.getImporto());
            totaleIva.v = totaleIva.v.add(oIva.getImporto());

            tableBuilder.addRow(Row.builder()
                    .add(TextCell.builder().text(notNull(o.getNumeroDocumento())).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(format(o.getDataDocumento())).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(notNull(o.getBeneficiario())).horizontalAlignment(LEFT).borderWidth(1).build())
                    .add(TextCell.builder().text(notNull(o.getFatturaNumero())).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(format(o.getFatturaData())).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(notNull(o.getNumeroDocumento())).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(format(o.getDataPagamento())).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(notNull(oIva.getNumeroPagamento())).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(format(oIva.getDataPagamento())).horizontalAlignment(CENTER).borderWidth(1).build())
                    .add(TextCell.builder().text(format(o.getImporto())).textColor(o.getImporto().compareTo(BigDecimal.ZERO) < 0 ? Color.RED : Color.BLACK).borderWidth(1).build())
                    .add(TextCell.builder().text(format(oIva.getImporto())).textColor(oIva.getImporto().compareTo(BigDecimal.ZERO) < 0 ? Color.RED : Color.BLACK).borderWidth(1).build())
                    .backgroundColor(i.v++ % 2 == 0 ? BLUE_LIGHT_1 : BLUE_LIGHT_2)
                    .horizontalAlignment(RIGHT)
                    .build());
        });

        // Add a final row
        tableBuilder.addRow(Row.builder()
                .add(TextCell.builder().text("TOTALI:")
                        .colSpan(9)
                        .lineSpacing(1f)
                        .borderWidthTop(1)
                        .textColor(WHITE)
                        .backgroundColor(BLUE_DARK)
                        .fontSize(10)
                        .font(new PDType1Font(HELVETICA_BOLD))
                        .borderWidth(1)
                        .build())
                .add(TextCell.builder().text("€ " + format(totaleImp.v)).backgroundColor(LIGHT_GRAY)
                        .font(new PDType1Font(HELVETICA_BOLD))
                        .verticalAlignment(TOP)
                        .fontSize(9)
                        .borderWidth(1)
                        .build())
                .add(TextCell.builder().text("€ " + format(totaleIva.v)).backgroundColor(LIGHT_GRAY)
                        .font(new PDType1Font(HELVETICA_BOLD))
                        .verticalAlignment(TOP)
                        .fontSize(9)
                        .borderWidth(1)
                        .build())
                .horizontalAlignment(RIGHT)
                .build());

        try (PDDocument document = new PDDocument()) {
            final PDPage page = new PDPage(A3_LANDSCAPE);
            document.addPage(page);
            float startY = page.getMediaBox().getHeight() - PADDING;
            try (final PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                String title = "DETTAGLIO PAGAMENTO IVA MESE DI " + mese + " " + anno;
                int fontSize = 14;
                int marginTop = 20;
                PDFont font = new PDType1Font(HELVETICA_BOLD);
                float titleWidth = font.getStringWidth(title) / 1000 * fontSize;
                float titleHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.newLineAtOffset((page.getMediaBox().getWidth() - titleWidth) / 2, page.getMediaBox().getHeight() - marginTop - titleHeight);
                contentStream.showText(title);
                contentStream.endText();

                Table table = tableBuilder.build();
                TableDrawer.builder()
                        .page(page)
                        .contentStream(contentStream)
                        .table(table)
                        //.startX(PADDING)
                        .startX((page.getMediaBox().getWidth() - table.getWidth()) / 2)
                        .startY(startY)
                        .endY(PADDING)
                        .build()
                        .draw(() -> document, () -> new PDPage(A3_LANDSCAPE), PADDING);
            }

            String outputFileName = UUID.randomUUID().toString() + ".pdf";
            document.save(documentFolder + "/" + outputFileName);
            return outputFileName;
        }
    }

    private String format(LocalDate d) {
        return (d != null) ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    private String format(BigDecimal d) {
        return (d != null) ? new DecimalFormat("#,##0.00").format(d) : "";
    }
    
    public BigDecimal getTotaleOrdinativiPeriodo(LocalDate min, LocalDate max) {
        String sql = "SELECT sum(importo) as tot from ordinativo o where (o.data_pagamento between {0} and {1})";
        return ctx.fetchSingle(sql, min, max).into(BigDecimal.class);
    }
    
    public String notNull(String s) {
        return s!=null ? s : "";
    }

    @LogDatabaseOperation
    public int concludiIVARitenuta(OrdinativoRecord o, Documento documento) {
        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(documento.getFileName());
        
        try {                           
            AtomicInteger num = new AtomicInteger(0);
            ctx.transaction(trx -> {
                try {                                    
                    List<OrdinativoRecord> oF24 = trx.dsl().selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.NUMERO_PAGAMENTO.eq(o.getDescrizioneRts())).fetch(); // descrizioneRts = End2EndID
                    if(!oF24.isEmpty()) {
                        // Salva il documento sul disco
                        Files.write(Paths.get(documentFolder + "/" + localFileName), documento.getContent());
                        logger.info("Il documento [{}] con nome logico [{}] è stato correttamente aggiunto al volume.", localFileName, documento.getFileName());
                        
                        // a ciascun ordinativo F24 associa il documento ortes di chiusura
                        oF24.forEach(oi -> {
                            AllegatoRecord a = new AllegatoRecord(null, oi.getId(), documento.getGruppo(), documento.getFileName(), localFileName, "Contabile Or.Te.S.");
                            trx.dsl().insertInto(Tables.ALLEGATO).set(a).execute();
                        });
                        
                        // Estrae l'elenco degli ordinativi F24 pagati
                        List<OrdinativoRecord> lPagati = trx.dsl().selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.NUMERO_PAGAMENTO.eq(o.getDescrizioneRts())).fetch();
                        
                        int totUpd = 0;                        
                        for(OrdinativoRecord p : lPagati) {
                            // Scarta il riepilogo erario (non ha alcun ordinativo collegato)
                            logger.info("Processo l'ordinativo collegato di {}", p);
                            if(p.getImporto().equals(BigDecimal.ZERO) && (p.getFatturaNumero()==null && p.getFatturaData()==null)) {
                                logger.info("Scarto l'ordinativo [ID={}] in quanto riepilogo ERARIO.", p.getId());
                                // conta come aggiornato
                                totUpd++;
                                continue;
                            }
                            
                            // per ciascuno recupera l'ordinativo che paga (prima prova l'iva)
                            OrdinativoRecord _o = trx.dsl().selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.ORDINATIVO_IVA.eq(p.getId())).fetchOne();
                            if(_o==null) {
                                // non trovato, prova la ritenuta
                                _o = trx.dsl().selectFrom(Tables.ORDINATIVO).where(Tables.ORDINATIVO.ORDINATIVO_RITENUTA.eq(p.getId())).fetchOne();
                                if(_o==null) throw new IntegrityException("Non è stato possibile trovare l'ordinativo IVA o RITENUTA di riferimento (ID="+p.getId()+").");
                            }
                            
                            // inverte il flag di "attesa" (posizione 1)
                            int flag = bitUnset(_o.getFlag(), 1);
                            // recupera e aggiorna il numero di versione
                            long oldV = _o.getVersione();                            
                             
                            // aggiorna il record con il nuovo flag e la nuova versione
                            totUpd += trx.dsl().update(Tables.ORDINATIVO)
                                                        .set(Tables.ORDINATIVO.FLAG, flag)
                                                        .set(Tables.ORDINATIVO.RTS_COMPLETO, (byte)1)
                                                        .set(Tables.ORDINATIVO.VERSIONE, oldV+1)
                                                .where(Tables.ORDINATIVO.ID.eq(_o.getId()).and(Tables.ORDINATIVO.VERSIONE.eq(oldV))).execute();
                        }
                        
                        // controlla che siano stati aggiornati tutti i record
                        if(totUpd!=lPagati.size()) throw new IntegrityException("Il numero di ordinativi aggiornati differisce dal numero di quelli da aggiornare. Proabile aggiornamento di qualche ordinativo da parte di un altro utente. Riprovare.");
                        
                        // aggiorna gli ordinativi F24 sostituendo numero e data con i dati dell'ortes
                        int res = trx.dsl().update(Tables.ORDINATIVO).set(Tables.ORDINATIVO.NUMERO_PAGAMENTO, o.getNumeroPagamento())
                                                                     .set(Tables.ORDINATIVO.DATA_PAGAMENTO, o.getDataPagamento())
                                                                     .set(Tables.ORDINATIVO.RTS_COMPLETO, (byte)1)
                                                                     .set(Tables.ORDINATIVO.VERSIONE, Tables.ORDINATIVO.VERSIONE.plus(1))
                                           .where(Tables.ORDINATIVO.NUMERO_PAGAMENTO.eq(o.getDescrizioneRts())).execute();
                        
                        // restituisce il numero di record aggiornati
                        num.set(res);
                    }                                                    
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                } 
            }); 
            
            return num.get();
        } catch (DataAccessException | IntegrityException e) {            
            try {
                Files.delete(Paths.get(documentFolder + "/" + localFileName));
                logger.info("Il documento [{}] è stato correttamente rimosso dal volume.", localFileName);
            } catch (IOException ioe) {
            }
            
            throw e;
        }
    }        
    
    public int bitSet(int value, int pos) {
        return value | (1 << pos);
    }
    
    public int bitUnset(int value, int pos) {
        return value & ~(1 << pos);
    }   
}
