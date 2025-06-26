/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.Tables;
import it.usr.web.usrbilancio.domain.tables.records.ParametriRecord;
import it.usr.web.usrbilancio.producer.DSLBilancio;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Startup
@Singleton
public class VersionService {

    public final static String DEPLOY_PARAM = "deploy";
    public final static String VERSION_PATTERN = "yyyyMMdd";
    @Inject
    @DSLBilancio
    DSLContext ctx;
    String currentVersion;
    @Inject
    @AppLogger
    Logger logger;

    @PostConstruct
    public void init() {
        try {
            ParametriRecord pr = ctx.selectFrom(Tables.PARAMETRI).where(Tables.PARAMETRI.PARAMETRO.eq(DEPLOY_PARAM)).fetchOne();
            if (pr == null) {
                pr = new ParametriRecord();
                pr.setParametro(DEPLOY_PARAM);
                pr.setValore(getNextVersion(null));
                ctx.insertInto(Tables.PARAMETRI).set(pr).execute();
                logger.warn("Attenzione: nessuna versione trovata, generazione primo deploy.");
            } 
            else {
                logger.warn("Versione deploy precedente [{}].", pr.getValore());
                pr.setValore(getNextVersion(pr.getValore()));
                ctx.update(Tables.PARAMETRI).set(pr).where(Tables.PARAMETRI.PARAMETRO.eq(DEPLOY_PARAM)).execute();
            }
            currentVersion = pr.getValore();
            logger.info("Versione deploy attuale [{}].", currentVersion);
        } 
        catch (DataAccessException e) {
            logger.error("Impossibile reperire la versione di deploy a causa di ", e);
            currentVersion = "-";
        }
    }

    private String getNextVersion(String current) {
        current = current!=null ? current.trim() : "";
        String[] v = current.split("\\.");
        int build = 1;
        if (v.length == 2) {
            try {
                build = Integer.parseInt(v[1]);
                build++;
            } 
            catch (NumberFormatException nfe) {
                logger.warn("Nessun build-number associato alla versione. Partenza da 1.");
            }
        }
        String now = LocalDate.now().format(DateTimeFormatter.ofPattern(VERSION_PATTERN));
        if (!now.equalsIgnoreCase(v[0])) {
            v[0] = now;
            build = 1;
        }
        return v[0] + "." + build;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}
