/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.AllegatoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.MovimentiVirtualiRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoAppoggioRecord;
import it.usr.web.usrbilancio.interceptor.LogDatabaseOperation;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import it.usr.web.usrbilancio.producer.DocumentFolder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.time.LocalDate;
import org.apache.commons.io.FilenameUtils;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class OrdinativoAppoggioService {

    @DSLBilancio
    @Inject
    DSLContext ctx;
    @DocumentFolder
    @Inject
    String documentFolder;

    public List<OrdinativoAppoggioRecord> getOrdinativi() {
        return ctx.selectFrom(Tables.ORDINATIVO_APPOGGIO).orderBy(Tables.ORDINATIVO_APPOGGIO.DATA_ELABORAZIONE.desc()).fetch();
    }

    public List<OrdinativoAppoggioRecord> getOrdinativiUtente(String userName) {
        return ctx.selectFrom(Tables.ORDINATIVO_APPOGGIO).where(Tables.ORDINATIVO_APPOGGIO.PROPRIETARIO.eq(userName)).orderBy(Tables.ORDINATIVO_APPOGGIO.DATA_ELABORAZIONE.desc()).fetch();
    }

    public List<OrdinativoAppoggioRecord> getOrdinativiDataUtente(LocalDate d, String userName) {
        return ctx.selectFrom(Tables.ORDINATIVO_APPOGGIO).where(Tables.ORDINATIVO_APPOGGIO.PROPRIETARIO.eq(userName).and(Tables.ORDINATIVO_APPOGGIO.DATA_ELABORAZIONE.eq(d))).orderBy(Tables.ORDINATIVO_APPOGGIO.DATA_ELABORAZIONE.desc()).fetch();
    }
    
    public List<AllegatoAppoggioRecord> getAllegatiOrdinativoAppoggio(int idOrdinativoAppoggio) {
        return ctx.selectFrom(Tables.ALLEGATO_APPOGGIO).where(Tables.ALLEGATO_APPOGGIO.ID_ORDINATIVO_APPOGGIO.eq(idOrdinativoAppoggio)).fetch();
    }

    public AllegatoAppoggioRecord getAllegatoAppoggioById(int idAllegatoAppoggio) {
        return ctx.selectFrom(Tables.ALLEGATO_APPOGGIO).where(Tables.ALLEGATO_APPOGGIO.ID.eq(idAllegatoAppoggio)).fetchOne();
    }

    @LogDatabaseOperation
    public void inserisci(OrdinativoAppoggioRecord oa, List<Documento> documenti) {
        List<AllegatoAppoggioRecord> all = new ArrayList<>();

        try {
            ctx.transaction(trx -> {
                trx.dsl().insertInto(Tables.ORDINATIVO_APPOGGIO).set(oa).execute();
                int idOrdinativoApp = trx.dsl().lastID().intValue();

                try {
                    for (Documento doc : documenti) {
                        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
                        Files.write(Paths.get(documentFolder + "/" + localFileName), doc.getContent());

                        AllegatoAppoggioRecord a = new AllegatoAppoggioRecord(null, idOrdinativoApp, doc.getGruppo(), doc.getFileName(), localFileName, "PDF Ordinativo");
                        trx.dsl().insertInto(Tables.ALLEGATO_APPOGGIO).set(a).execute();
                        all.add(a);
                    }
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }
            });
        } catch (DataAccessException dae) {
            try {
                for (AllegatoAppoggioRecord a : all) {
                    if (a.getNomefileLocale() != null) {
                        Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
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
    public void inserisci(List<OrdinativoAppoggioRecord> lOa, Documento documento) {
        List<AllegatoAppoggioRecord> all = new ArrayList<>();

        try {
            ctx.transaction(trx -> {
                lOa.forEach(oa -> {
                    trx.dsl().insertInto(Tables.ORDINATIVO_APPOGGIO).set(oa).execute();
                    int idOrdinativoApp = trx.dsl().lastID().intValue();
                    
                    try {                    
                        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(documento.getFileName());
                        Files.write(Paths.get(documentFolder + "/" + localFileName), documento.getContent());

                        AllegatoAppoggioRecord a = new AllegatoAppoggioRecord(null, idOrdinativoApp, documento.getGruppo(), documento.getFileName(), localFileName, "PDF Ordinativo");
                        trx.dsl().insertInto(Tables.ALLEGATO_APPOGGIO).set(a).execute();
                        all.add(a);                 
                    } catch (IOException ioe) {
                        throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                    }
                });                
            });
        } catch (DataAccessException dae) {
            try {
                for (AllegatoAppoggioRecord a : all) {
                    if (a.getNomefileLocale() != null) {
                        Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
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
    public void modifica(OrdinativoAppoggioRecord oa) {
        int upd = ctx.update(Tables.ORDINATIVO_APPOGGIO).set(oa).where(Tables.ORDINATIVO_APPOGGIO.ID.eq(oa.getId())).execute();
        if (upd != 1) {
            throw new StaleRecordException("L'ordinativo di apoggio [" + oa.getId() + "] è eliminato.");
        }
    }

    @LogDatabaseOperation
    public void elimina(OrdinativoAppoggioRecord oa) {
        List<AllegatoAppoggioRecord> la = new ArrayList<>();
        ctx.transaction(trx -> {
            la.addAll(trx.dsl().selectFrom(Tables.ALLEGATO_APPOGGIO).where(Tables.ALLEGATO_APPOGGIO.ID_ORDINATIVO_APPOGGIO.eq(oa.getId())).fetch());

            trx.dsl().delete(Tables.ALLEGATO_APPOGGIO).where(Tables.ALLEGATO_APPOGGIO.ID_ORDINATIVO_APPOGGIO.eq(oa.getId())).execute();
            trx.dsl().delete(Tables.ORDINATIVO_APPOGGIO).where(Tables.ORDINATIVO_APPOGGIO.ID.eq(oa.getId())).execute();
        });

        for (AllegatoAppoggioRecord aa : la) {
            try {
                Files.delete(Paths.get(documentFolder + "/" + aa.getNomefileLocale()));
            } catch (IOException ioe) {
            }
        }
    }

    @LogDatabaseOperation
    public void aggiungi(List<Documento> documenti, int idOrdinativoApp) {
        List<AllegatoAppoggioRecord> all = new ArrayList<>();

        try {
            ctx.transaction(trx -> {
                try {
                    for (Documento doc : documenti) {
                        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
                        Files.write(Paths.get(documentFolder + "/" + localFileName), doc.getContent());

                        AllegatoAppoggioRecord a = new AllegatoAppoggioRecord(null, idOrdinativoApp, doc.getGruppo(), doc.getFileName(), localFileName, doc.getContentType());
                        trx.dsl().insertInto(Tables.ALLEGATO_APPOGGIO).set(a).execute();
                        all.add(a);
                    }
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }
            });
        } catch (DataAccessException dae) {
            try {
                for (AllegatoAppoggioRecord a : all) {
                    if (a.getNomefileLocale() != null) {
                        Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
                    }
                }
            } catch (IOException e) {
            }

            throw dae;
        }
    }

    @LogDatabaseOperation
    public void modifica(AllegatoAppoggioRecord a) {
        ctx.update(Tables.ALLEGATO_APPOGGIO).set(a).where(Tables.ALLEGATO_APPOGGIO.ID.eq(a.getId())).execute();
    }

    @LogDatabaseOperation
    public void elimina(AllegatoAppoggioRecord allegato) {
        int count = ctx.delete(Tables.ALLEGATO_APPOGGIO).where(Tables.ALLEGATO_APPOGGIO.ID.eq(allegato.getId())).execute();
        if (count != 1) {
            throw new StaleRecordException("Allegato di appoggio [" + allegato.getId() + "] già rimosso");
        }
        try {
            if (allegato.getNomefileLocale() != null) {
                Files.delete(Paths.get(documentFolder + "/" + allegato.getNomefileLocale()));
            }
        } catch (IOException e) {
        }
    }

    @LogDatabaseOperation
    public void trasformaMovimento(OrdinativoAppoggioRecord oa, List<Documento> documenti, MovimentiVirtualiRecord mv) {
        List<AllegatoAppoggioRecord> all = new ArrayList<>();

        try {
            ctx.transaction(trx -> {
                trx.dsl().insertInto(Tables.ORDINATIVO_APPOGGIO).set(oa).execute();
                int idOrdinativoApp = trx.dsl().lastID().intValue();

                try {
                    for (Documento doc : documenti) {
                        String localFileName = UUID.randomUUID().toString() + "." + FilenameUtils.getExtension(doc.getFileName());
                        Files.write(Paths.get(documentFolder + "/" + localFileName), doc.getContent());

                        AllegatoAppoggioRecord a = new AllegatoAppoggioRecord(null, idOrdinativoApp, doc.getGruppo(), doc.getFileName(), localFileName, "PDF Ordinativo");
                        trx.dsl().insertInto(Tables.ALLEGATO_APPOGGIO).set(a).execute();
                        all.add(a);
                    }
                } catch (IOException ioe) {
                    throw new UploadException("Impossibile caricare il file a causa di " + ioe.getMessage());
                }

                // Aggiunge all'elenco l'eventuale documento allegato al movimento virtuale
                if(mv.getNomefile()!=null && mv.getNomefileLocale()!=null) {
                    AllegatoAppoggioRecord a = new AllegatoAppoggioRecord(null, idOrdinativoApp, null, mv.getNomefile(), mv.getNomefileLocale(), "Documento allegato al movimento virtuale");
                    trx.dsl().insertInto(Tables.ALLEGATO_APPOGGIO).set(a).execute();
                }
                
                int cnt = trx.dsl().deleteFrom(Tables.MOVIMENTI_VIRTUALI).where(Tables.MOVIMENTI_VIRTUALI.ID.eq(mv.getId())).execute();
                if (cnt != 1) {
                    throw new IntegrityException("Il movimento virtuale non è più disponibile.");
                }
            });
        } catch (DataAccessException dae) {
            try {
                for (AllegatoAppoggioRecord a : all) {
                    if (a.getNomefileLocale() != null) {
                        Files.delete(Paths.get(documentFolder + "/" + a.getNomefileLocale()));
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
}
