/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ApplicationScoped
public class TableExporterController extends BaseController {
    @Inject
    CompetenzaService cs;
    @Inject
    CodiceService codServ;
    Map<Integer, CapitoloCompetenza> capComp;
    Map<Integer, CodiceRecord> codici;
    Map<Integer, TipoDocumentoRecord> documenti;
    Map<Integer, TipoRtsRecord> rts;
    
    @PostConstruct
    public void init() {
        capComp = new HashMap<>();
        codici = new HashMap<>();
        documenti = new HashMap<>();
        rts = new HashMap<>();
                 
        cs.getCapitoliCompetenze().forEach(cc -> capComp.put(cc.getId(), cc));
        codServ.getCodici().forEach(c -> codici.put(c.getId(), c));
        codServ.getTipiDocumento().forEach(t -> documenti.put(t.getId(), t));
        codServ.getTipiRts(CodiceService.GruppoRts.RTS_TUTTI).forEach(t -> rts.put(t.getId(), t));
    }
    
    public String decodeCompetenza(int idCapComp) {
        CapitoloCompetenza cc = capComp.get(idCapComp);
        return (cc!=null) ? cc.getAnno()+" | "+cc.getDescrizione() : null;
    }
    
    public String decodeConto(int idCodice) {
        CodiceRecord cr = codici.get(idCodice);
        return (cr!=null) ? Formatter.formattaCodice(cr) : null;
    }
    
    public String decodeStato(byte stato) {
        return switch(stato) {
            case 0 -> "APERTO";
            case 1 -> "CHIUSO";
            case 2 -> "IN CHIUSURA";
            default -> "?";
        };
    }
    
    public String decodeDocumento(int idTipoDocumento) {
        TipoDocumentoRecord t = documenti.get(idTipoDocumento);
        return (t!=null) ? t.getDescrizione() : null;
    }
    
    public String decodeRts(int idTipoRts) {
        TipoRtsRecord t = rts.get(idTipoRts);
        return (t!=null) ? t.getCodice() : null;
    }
    
    public String decodeImpoto(BigDecimal d) {
        DecimalFormat f = new DecimalFormat("0.00");
        return (d!=null) ? f.format(d) : null;
    }
    
    public String decodeDate(LocalDate d) {
        return (d!=null) ? d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : null;
    }
    
    public String decodePagato(boolean pagato) {
        return pagato ? "SI" : "NO";            
    }
}
