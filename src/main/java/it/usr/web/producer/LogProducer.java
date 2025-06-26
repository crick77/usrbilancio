/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.usr.web.producer;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Crick
 */
@Startup
@Singleton
public class LogProducer {
    private final static String LOG_OUTPUT_DIR = "LOG_OUTPUT_DIR";
    private final static String LOG_OUTPUT_FILENAME = "LOG_OUTPUT_FILENAME";
    @Resource(lookup="usrbilancio/logOutputDir")
    String outputDir;
    @Resource(lookup="usrbilancio/logOutputFileName")
    String outputFileName;
    
    @PostConstruct
    public final void setup() {
        try {
            LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
            JoranConfigurator jc = new JoranConfigurator();
            jc.setContext(context);
            context.reset();
            if(getOutputDir()!=null) context.putProperty(LOG_OUTPUT_DIR, getOutputDir());
            if(getOutputFileName()!=null) context.putProperty(LOG_OUTPUT_FILENAME, getOutputFileName());
            jc.doConfigure(this.getClass().getClassLoader().getResourceAsStream("logback.xml"));
            
            System.out.println("Logger initialized. Context OutputDir: ["+getOutputDir()+"], Context OutputFileName: ["+getOutputFileName()+"].");
        } catch (JoranException ex) {
            System.err.println("init logging failed: "+ex);
        }
    }

    /**
     * Restituisce il logger relativo alla classe indicata dall'injection point
     * passato per argomento
     *
     * @param ip l'injection point su cui applicare il logger
     * @return il logger associato
     */
    @Produces
    @AppLogger
    public Logger produceLogger(InjectionPoint ip) {
        return LoggerFactory.getLogger(ip.getMember().getDeclaringClass());
    }
    
    /**
     * Restituisce la cartella dove collogare i logs
     * 
     * @return il percorso della cartella
     */
    public String getOutputDir() {
        return this.outputDir;
    }
    
    /**
     * Restituisce il nome del file dei logs
     * 
     * @return il nome del file
     */
    public String getOutputFileName() {
        return this.outputFileName;
    }
}
