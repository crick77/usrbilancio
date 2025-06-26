/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.pdfextract.model.QuietanzaGeocos;
import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.PDFExtractor;
import it.usr.web.usrbilancio.service.QuietanzaService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class ImportaQuietanzaController extends BaseController {
    @Inject
    CompetenzaService cs;
    @Inject
    QuietanzaService qs;
    @Inject
    CodiceService codServ;
    @Inject
    @AppLogger
    Logger logger;  
    @Inject
    PDFExtractor pe;
    QuietanzaRecord quietanza;
    UploadedFile file;
    CapitoloCompetenza quietanzaCapComp;
    CodiceRecord quietanzaCodice;
    TipoRtsRecord quietanzaTipoRts;
    TipoDocumentoRecord quietanzaTipoDocumento;
    List<CapitoloCompetenza> capComp;
    CapitoloCompetenza capCompSelezionato;
    Map<Integer, CapitoloCompetenza> mCampComp;
    Map<Integer, CodiceRecord> codici;
    Map<Integer, TipoRtsRecord> tipiRts;
    List<TipoRtsRecord> tipiRtsList;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    
    public void init() {
        codici = codServ.getCodiciAsMap();
        tipiRtsList = codServ.getTipiRts(CodiceService.GruppoRts.RTS_QUIETANZA);
        tipiRts = new HashMap<>();
        tipiRtsList.forEach(t -> tipiRts.put(t.getId(), t));        
        List<TipoDocumentoRecord> lTipoDoc = codServ.getTipiDocumentoNuovi();
        tipiDocumento = new HashMap<>();
        lTipoDoc.forEach(d -> tipiDocumento.put(d.getId(), d));
        capComp = cs.getCapitoliCompetenzeApertiNonFuturi();  
        mCampComp = new HashMap<>();
        capComp.forEach(cc -> mCampComp.put(cc.getId(), cc));
        
        quietanza = null;
        file = null;
    }

    public QuietanzaRecord getQuietanza() {
        return quietanza;
    }

    public void setQuietanza(QuietanzaRecord quietanza) {
        this.quietanza = quietanza;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public CapitoloCompetenza getQuietanzaCapComp() {
        return quietanzaCapComp;
    }

    public void setQuietanzaCapComp(CapitoloCompetenza quietanzaCapComp) {
        this.quietanzaCapComp = quietanzaCapComp;
    }

    public CodiceRecord getQuietanzaCodice() {
        return quietanzaCodice;
    }

    public void setQuietanzaCodice(CodiceRecord quietanzaCodice) {
        this.quietanzaCodice = quietanzaCodice;
    }

    public TipoRtsRecord getQuietanzaTipoRts() {
        return quietanzaTipoRts;
    }

    public void setQuietanzaTipoRts(TipoRtsRecord quietanzaTipoRts) {
        this.quietanzaTipoRts = quietanzaTipoRts;
    }

    public TipoDocumentoRecord getQuietanzaTipoDocumento() {
        return quietanzaTipoDocumento;
    }

    public void setQuietanzaTipoDocumento(TipoDocumentoRecord quietanzaTipoDocumento) {
        this.quietanzaTipoDocumento = quietanzaTipoDocumento;
    }

    public List<CapitoloCompetenza> getCapComp() {
        return capComp;
    }

    public void setCapComp(List<CapitoloCompetenza> capComp) {
        this.capComp = capComp;
    }

    public CapitoloCompetenza getCapCompSelezionato() {
        return capCompSelezionato;
    }

    public void setCapCompSelezionato(CapitoloCompetenza capCompSelezionato) {
        this.capCompSelezionato = capCompSelezionato;
    }

    public Map<Integer, CapitoloCompetenza> getmCampComp() {
        return mCampComp;
    }

    public void setmCampComp(Map<Integer, CapitoloCompetenza> mCampComp) {
        this.mCampComp = mCampComp;
    }

    public Map<Integer, CodiceRecord> getCodici() {
        return codici;
    }

    public void setCodici(Map<Integer, CodiceRecord> codici) {
        this.codici = codici;
    }

    public Map<Integer, TipoRtsRecord> getTipiRts() {
        return tipiRts;
    }

    public List<TipoRtsRecord> getTipiRtsList() {
        return tipiRtsList;
    }
    
    public void setTipiRts(Map<Integer, TipoRtsRecord> tipiRts) {
        this.tipiRts = tipiRts;
    }

    public Map<Integer, TipoDocumentoRecord> getTipiDocumento() {
        return tipiDocumento;
    }

    public void setTipiDocumento(Map<Integer, TipoDocumentoRecord> tipiDocumento) {
        this.tipiDocumento = tipiDocumento;
    }
        
    public void elabora() {
        try {
            quietanza = null;
            
            logger.info("Importazione quietanza dal file [{}] tipo [{}] dimensione [{}].", file.getFileName(), file.getContentType(), file.getSize());
            QuietanzaGeocos qc = pe.buildQuietanza(file.getContent());
            if(qc==null) {
                addMessage(Message.error("Errore nell'elaborazione del file. Probabile documento NON GeoCos."));
                return;
            }
            logger.info("Decodifica quietanza dal file [{}] -> {}", file.getFileName(), qc);
            
            quietanza = new QuietanzaRecord();
            quietanza.setImporto(qc.getImporto());
            quietanza.setNumeroPagamento(qc.getNumeroQuietanza());
            quietanza.setDataPagamento(toLocalDate(qc.getDataContabile()));
            quietanza.setOrdinante(qc.getVersante());
            quietanza.setNote(qc.getCausale());
            if(notNull(qc.getVersante()).toUpperCase().startsWith("COM.STR.") || notNull(qc.getVersante()).toUpperCase().startsWith("COMMISSARIO")) {
                quietanza.setFlag(1);
            }
            quietanza.setVersione(1L);                       
        }
        catch(Exception e) {
            logger.error("Impossibile elaborare il file {} a causa di {}", file.getFileName(), e.toString());
            addMessage(Message.error("Impossibile elaborare il file per un errore inatteso: "+e.toString()));
        }
    }
    
    public void salva() { 
        try {
            Documento doc = new Documento(file.getFileName(), null, file.getContent(), codServ.getMimeType(file.getContentType()));
            quietanza.setIdCodice(quietanzaCodice.getId());
            quietanza.setIdCompetenza(quietanzaCapComp.getId());
            quietanza.setIdTipoRts(quietanzaTipoRts.getId());            
            quietanza.setIdTipoDocumento(quietanzaTipoDocumento!=null ? quietanzaTipoDocumento.getId() : null);
            qs.inserisci(quietanza, doc);
            
            annulla();
            
            addMessage(Message.info("Quietanza salvata con successo."));
        }
        catch(Exception e) {
            logger.error("Impossibile salvare la quietanza a causa di {}", e.toString());
            addMessage(Message.error("Impossibile salvare la quietanza per un errore inatteso: "+e.toString()));
        }
    }            
    
    public void annulla() {
        file = null;
        quietanza = null;
        quietanzaCodice = null;
        quietanzaCapComp = null;
        quietanzaTipoDocumento = null;
        quietanzaTipoRts = null;
    }
}
