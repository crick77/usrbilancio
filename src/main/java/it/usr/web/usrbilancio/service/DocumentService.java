/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.model.Documento;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DocumentService {
    @Inject
    @AppLogger
    Logger logger;
    
    public Documento extractP7M(String fileName) throws Exception {
        Path file = Paths.get(fileName);
        byte[] p7mFileByteArray = Files.readAllBytes(file);
        
        return extractP7M(file.getFileName().toString(), null, p7mFileByteArray);
    }

    public Documento extractP7M(Documento d) throws Exception {
        return extractP7M(d.getFileName(), d.getGruppo(), d.getContent());
    }
    
    public Documento extractP7M(String fileName, String gruppo, byte[] p7mFileByteArray) throws Exception { 
        String originalFileName = fileName;
        int iteration = 1;
        while (fileName.toLowerCase().endsWith("p7m")) {            
            try {
                p7mFileByteArray = getData(p7mFileByteArray);
                if(p7mFileByteArray==null) return null;

                fileName = FilenameUtils.removeExtension(fileName);
                iteration++;
            }
            catch(CMSException e) {
                logger.error("Impossibile estrarre il file P7M [{}] all'iterazione {} a causa di {}.", originalFileName, iteration, e);
                throw e;
            }            
        }
        
        return new Documento(fileName, gruppo, p7mFileByteArray, "Versione non firmata di "+originalFileName);
    }
    
    private byte[] getData(final byte[] p7bytes) throws CMSException {        
        CMSSignedData cms = new CMSSignedData(p7bytes);
        return (cms.getSignedContent() != null) ? (byte[])cms.getSignedContent().getContent() : null;                
    }
}
