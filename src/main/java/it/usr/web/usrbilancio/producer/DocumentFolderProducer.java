/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.producer;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;

/**
 *
 * @author riccardo.iovenitti
 */
public class DocumentFolderProducer {
    @Resource(lookup = "usrbilancio/documentFolder")
    String documentFolder;
    
    @Produces
    @DocumentFolder
    public String getDocumentFolder() {
        return documentFolder;
    }
}
