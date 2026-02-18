/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import static it.usr.web.controller.BaseController.DATE_PATTERN_LONG;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.MovimentiVirtualiRecord;
import it.usr.web.usrbilancio.domain.tables.records.QuietanzaRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.DuplicationException;
import it.usr.web.usrbilancio.service.MovimentiVirtualiService;
import it.usr.web.usrbilancio.service.PDFExtractor;
import it.usr.web.usrbilancio.service.QuietanzaService;
import it.usr.web.usrbilancio.service.StaleRecordException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.ejb.EJBException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.primefaces.PrimeFaces;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.util.LangUtils;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class MovimentiCompletiController extends BaseController {
    private final static String[] CAUSALE = {"Causale beneficiario:", "Descrizione:"};
    private final static String[] ORDINANTE = {"Descrizione ordinante:", "Data disposizione:"};
    private final static String[] DATA_CONTABILE = {"Data contabile:", "Data di riferimento:"};
    private final static String[] IMPORTO = {"Importo:", "Data contabile:"};
    private final static String[] ID = {"Numero quietanza:", "Indicatore fruttifero"};
    private final static String[] ID_LONG = {"Numero quietanza:", "Indicatore fruttifero infruttifero:"};
    @Inject
    PDFExtractor pe;
    @Inject
    MovimentiVirtualiService mvs;
    @Inject
    CompetenzaService cs;
    @Inject
    CodiceService codServ;
    //@Inject
    //OrdinativoAppoggioService oas;
    @Inject
    QuietanzaService qs;
    @Inject
    @AppLogger
    Logger logger;
    CapitoloCompetenza movimentoCapComp;
    CodiceRecord movimentoCodice;
    TipoDocumentoRecord movimentoTipoDocumento;
    List<CapitoloCompetenza> capComp;
    List<CapitoloCompetenza> capCompAperti;
    Map<Integer, CapitoloCompetenza> mCampComp;
    List<CodiceRecord> codici;
    Map<Integer, CodiceRecord> codiciMap;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    List<MovimentiVirtualiRecord> movimenti;
    List<MovimentiVirtualiRecord> movimentiFiltrati;
    List<TipoRtsRecord> tipiRts;
    TipoRtsRecord tipoRts;
    MovimentiVirtualiRecord movimento;
    String[] selectedCapitoli;
    List<String> capitoli;
    UploadedFile documento;
    BigDecimal totale;
    BigDecimal totaleParziale;
    String azione;
    Integer movimentoCodiceFiltro;
    boolean usaDescrizioneDocumento;
    boolean usaOrdinanteDocumento;
    int statoCapitoloFilter;    

    public void init() {
        codici = codServ.getCodici();
        codiciMap = new HashMap<>();
        codici.forEach(c -> codiciMap.put(c.getId(), c));
        capComp = cs.getCapitoliCompetenze();
        mCampComp = new HashMap<>();
        capComp.forEach(cc -> {
            mCampComp.put(cc.getId(), cc);
        });
        capCompAperti = cs.getCapitoliCompetenzeAperti();
        tipiDocumento = codServ.getTipiDocumentoAsMap();
        tipiRts = codServ.getTipiRts(CodiceService.GruppoRts.RTS_QUIETANZA);
        
        movimentoCapComp = null;
        movimentoCodice = null;
        movimentoTipoDocumento = null;
        totale = null;
        totaleParziale = null;
        documento = null;
        azione = null;
        selectedCapitoli = null;
        tipoRts = null;
        statoCapitoloFilter = -1; 
        usaDescrizioneDocumento = false;
        usaOrdinanteDocumento = false;
        movimentoCodiceFiltro = null;

        aggiornaMovimenti();
        clearFilters(false);
    }

    public Integer getMovimentoCodiceFiltro() {
        return movimentoCodiceFiltro;
    }

    public void setMovimentoCodiceFiltro(Integer movimentoCodiceFiltro) {
        this.movimentoCodiceFiltro = movimentoCodiceFiltro;
    }
        
    public boolean isUsaDescrizioneDocumento() {
        return usaDescrizioneDocumento;
    }

    public void setUsaDescrizioneDocumento(boolean usaDescrizioneDocumento) {
        this.usaDescrizioneDocumento = usaDescrizioneDocumento;
    }

    public boolean isUsaOrdinanteDocumento() {
        return usaOrdinanteDocumento;
    }

    public void setUsaOrdinanteDocumento(boolean usaOrdinanteDocumento) {
        this.usaOrdinanteDocumento = usaOrdinanteDocumento;
    }
                
    public TipoRtsRecord getTipoRts() {
        return tipoRts;
    }

    public void setTipoRts(TipoRtsRecord tipoRts) {
        this.tipoRts = tipoRts;
    }

    public List<TipoRtsRecord> getTipiRts() {
        return tipiRts;
    }
        
    public int getStatoCapitoloFilter() {
        return statoCapitoloFilter;
    }

    public void setStatoCapitoloFilter(int statoCapitoloFilter) {
        this.statoCapitoloFilter = statoCapitoloFilter;
    }
        
    public List<MovimentiVirtualiRecord> getMovimenti() {
        return movimenti;
    }

    public MovimentiVirtualiRecord getMovimento() {
        return movimento;
    }

    public void setMovimento(MovimentiVirtualiRecord movimento) {
        this.movimento = movimento;

        if (movimento != null) {
            movimentoCapComp = mCampComp.get(movimento.getIdCompetenza());
            movimentoCodice = codiciMap.get(movimento.getIdCodice());
            movimentoTipoDocumento = tipiDocumento.get(movimento.getIdTipoDocumento());
        }
    }

    public void modifica(MovimentiVirtualiRecord mvr) {
        this.movimento = mvr;
        azione = "Modifica";
    }

    public void mostra(MovimentiVirtualiRecord mvr) {
        this.movimento = mvr;
        azione = "Mostra";
    }

    public CapitoloCompetenza getMovimentoCapComp() {
        return movimentoCapComp;
    }

    public void setMovimentoCapComp(CapitoloCompetenza movimentoCapComp) {
        this.movimentoCapComp = movimentoCapComp;
    }

    public CodiceRecord getMovimentoCodice() {
        return movimentoCodice;
    }

    public void setMovimentoCodice(CodiceRecord movimentoCodice) {
        this.movimentoCodice = movimentoCodice;
    }

    public List<CapitoloCompetenza> getCapComp() {
        return capComp;
    }

    public void setCapComp(List<CapitoloCompetenza> capComp) {
        this.capComp = capComp;
    }

    public List<CapitoloCompetenza> getCapCompAperti() {
        return capCompAperti;
    }

    public void setCapCompAperti(List<CapitoloCompetenza> capCompAperti) {
        this.capCompAperti = capCompAperti;
    }

    public Map<Integer, CapitoloCompetenza> getmCampComp() {
        return mCampComp;
    }

    public void setmCampComp(Map<Integer, CapitoloCompetenza> mCampComp) {
        this.mCampComp = mCampComp;
    }

    public List<CodiceRecord> getCodici() {
        return codici;
    }

    public Map<Integer, TipoDocumentoRecord> getTipiDocumento() {
        return tipiDocumento;
    }

    public void setTipiDocumento(Map<Integer, TipoDocumentoRecord> tipiDocumento) {
        this.tipiDocumento = tipiDocumento;
    }

    public CodiceRecord decodeCodice(int idCodice) {
        return codiciMap.get(idCodice);
    }

    public TipoDocumentoRecord decodeTipoDocumento(int idTipoDocumento) {
        return tipiDocumento.get(idTipoDocumento);
    }

    public List<MovimentiVirtualiRecord> getMovimentiFiltrati() {
        return movimentiFiltrati;
    }

    public void setMovimentiFiltrati(List<MovimentiVirtualiRecord> movimentiFiltrati) {
        this.movimentiFiltrati = movimentiFiltrati;
    }
 
    public String[] getSelectedCapitoli() {
        return selectedCapitoli;
    }

    public void setSelectedCapitoli(String[] selectedCapitoli) {
        this.selectedCapitoli = selectedCapitoli;
    }

    public List<String> getCapitoli() {
        return capitoli;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }

    public BigDecimal getTotaleParziale() {
        return totaleParziale;
    }

    public void setTotaleParziale(BigDecimal totaleParziale) {
        this.totaleParziale = totaleParziale;
    }

    public TipoDocumentoRecord getMovimentoTipoDocumento() {
        return movimentoTipoDocumento;
    }

    public void setMovimentoTipoDocumento(TipoDocumentoRecord movimentoTipoDocumento) {
        this.movimentoTipoDocumento = movimentoTipoDocumento;
    }

    public UploadedFile getDocumento() {
        return documento;
    }

    public void setDocumento(UploadedFile documento) {
        this.documento = documento;
    }

    public List<TipoDocumentoRecord> getTipiDocumentoList() {
        return new ArrayList<>(tipiDocumento.values());
    }

    public String getAzione() {
        return azione;
    }
    
    public void aggiornaMovimenti() {
        movimenti = mvs.getMovimentiVirtuali(statoCapitoloFilter);
        totale = BigDecimal.ZERO;
        movimenti.forEach(m -> {
            totale = totale.add(m.getImporto());
        });
        totaleParziale = totale;
        movimento = null;
        movimentiFiltrati = null;
        azione = null;
    }

    public void aggiornaParziale() {
        totaleParziale = BigDecimal.ZERO;
        movimentiFiltrati.forEach(mf -> {
            totaleParziale = totaleParziale.add(mf.getImporto());
        });
    }

    public boolean filtroGlobale(Object value, Object filter, Locale locale) {
        MovimentiVirtualiRecord rec = (MovimentiVirtualiRecord) value;
        /*if(statoCapitoloFilter!=-1 && decodeCapComp(rec.getIdCompetenza()).getChiuso()!=statoCapitoloFilter) {
            return false;
        }*/
         
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (LangUtils.isBlank(filterText)) {
            return true; 
        }
                
        boolean match = contains(rec.getDescrizioneRagioneria(), filterText)
                || contains(rec.getNote(), filterText);
        if (isDate(filterText)) {
            LocalDate d = toDate(filterText);
            match = match || d.equals(rec.getDataDocumento())
                    || d.equals(rec.getDataPagamento());
        }
        match = match || contains(rec.getNumeroDocumento(), filterText);
        match = match || contains(rec.getNumeroPagamento(), filterText);
        match = match || contains(toStringFormat(rec.getImporto()), filterText);

        return match;
    }
 
    public void filtraStatoCapitolo() {
        aggiornaMovimenti();
    }
    
    public void salva() {
        if (!movimento.changed()) {
            aggiornaMovimenti();
            PrimeFaces.current().executeScript("PF('movimentoDialog').hide();");
            return;
        }

        movimento.setIdCompetenza(movimentoCapComp.getId());
        movimento.setIdCodice(movimentoCodice.getId());
        movimento.setIdTipoDocumento(movimentoTipoDocumento.getId());

        Documento doc = (documento != null) ? new Documento(documento.getFileName(), null, documento.getContent(), codServ.getMimeType(documento.getContentType())) : null;

        try {
            if (movimento.getId() == null) {
                mvs.inserisci(movimento, doc);
            } else {
                mvs.modifica(movimento, doc);
            }

            aggiornaMovimenti();

            PrimeFaces.current().executeScript("PF('movimentoDialog').hide();");
        } catch (EJBException ex) {
            if (ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn("Il record è stato aggiornato da un altro utente. Aggiornare e riprovare."));
            } else if (ex.getCausedByException() instanceof DuplicationException) {
                addMessage(Message.warn("Esiste già un movimento virtuale con i dati indicati."));
            } else {
                addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il salvataggio del movimento virtuale {}. Errrore: {}", ex.getCausedByException().getClass(), movimento, ex.getCausedByException());
            }
        }
    }

    public void nuovo() {
        movimento = new MovimentiVirtualiRecord();
        movimento.setVersione(0L);

        movimentoCapComp = null;
        movimentoTipoDocumento = null;
        movimentoCodice = null;
        documento = null;
        azione = "Nuovo";
    }

    public void elimina() {
        if (movimento != null) {
            try {
                mvs.elimina(movimento);

                aggiornaMovimenti();
            } catch (EJBException ex) {
                addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione del movimento virtuale {}. Errrore: {}", ex.getCausedByException().getClass(), movimento, ex.getCausedByException());
            }
        } 
    }

    public void duplica(MovimentiVirtualiRecord m) {         
        MovimentiVirtualiRecord mvr = m.copy();
        mvr.setId(null);
        mvr.setIdCompetenza(null);
        mvr.setDescrizioneRagioneria("COPIA-"+movimento.getDescrizioneRagioneria());
        movimentoCapComp = mCampComp.get(movimento.getIdCompetenza());
        mvr.setVersione(1L);
        movimento = mvr;
        azione = "Duplica";
    } 

    public CapitoloCompetenza decodeCapComp(int idCapComp) {
        return mCampComp.get(idCapComp);
    }

    public boolean isChiuso() {
        if (movimento != null) {
            CapitoloCompetenza cc = mCampComp.get(movimento.getIdCompetenza());
            return (cc != null) ? (cc.getChiuso() == 1 || cc.getChiuso() == 2) : false;
        } else {
            return false;
        }
    }

    public boolean chiuso(MovimentiVirtualiRecord mov) {
        if (mov != null) {
            CapitoloCompetenza cc = mCampComp.get(mov.getIdCompetenza());
            return (cc != null) ? (cc.getChiuso() == 1 || cc.getChiuso() == 2) : false;
        } else {
            return false;
        }
    }

    public boolean filterFunctionCapitoli(Object value, Object filter, Locale locale) {
        if (value == null || !(value instanceof String)) {
            return true;
        }
        String sVal = (String) value;
        if (selectedCapitoli == null || selectedCapitoli.length == 0) {
            return false;
        }
        for (String sel : selectedCapitoli) {
            if (sel.equalsIgnoreCase(sVal)) {
                return true;
            }
        }

        return false;
    }

    public void clearFilters(boolean resetFilters) {
        capitoli = new ArrayList<>();
        capComp.forEach(cc -> { 
            capitoli.add(cc.getAnno() + " | " + cc.getDescrizione());
        });
        selectedCapitoli = new String[capitoli.size()];
        capitoli.toArray(selectedCapitoli);
        statoCapitoloFilter = -1;
        movimentoCodiceFiltro = null;

        if (resetFilters) {
            PrimeFaces.current().executeScript("PF('movimentiTable').clearFilters();PF('capcompcbm').checkAll()");
        }
    }

    public void trasforma() {
        if (documento == null) {
            addMessage(Message.warn("Selezionare un file prima di importare!"));
            return;
        }
        
        try {
            logger.info("Importazione quietanza dal file [{}] tipo [{}] dimensione [{}].", documento.getFileName(), documento.getContentType(), documento.getSize());
            QuietanzaRecord qr = extractPDF(documento.getContent());
            if(qr==null) {
                addMessage(Message.error("Il documento caricato NON è una quietanza Or.TeS."));
                return;
            }
            logger.info("Decodifica quietanza dal file [{}] -> {}", documento.getFileName(), qr);
            
            if(!Objects.equals(qr.getImporto(), movimento.getImporto())) {
                addMessage(Message.error("L'importo del documento caricato NON corrisponde a quello del movimento che si sta trasformando."));
                return;
            }
             
            qr.setIdCompetenza(movimento.getIdCompetenza());
            qr.setIdCodice(movimento.getIdCodice());
            qr.setIdTipoDocumento(movimento.getIdTipoDocumento());
            qr.setNumeroDocumento(movimento.getNumeroDocumento());
            qr.setIdTipoRts(tipoRts.getId());
            qr.setDataDocumento(movimento.getDataDocumento());
            if(!usaDescrizioneDocumento) qr.setDescrizioneOrdinanza(movimento.getDescrizioneRagioneria());
            qr.setNote(movimento.getNote());
            if(!usaOrdinanteDocumento) qr.setOrdinante(movimento.getNominativo());
             
            Documento doc = new Documento(documento.getFileName(), null, documento.getContent(), null);
            
            qs.trasforma(qr, movimento, doc); 
            aggiornaMovimenti();
            
            addMessage(Message.info("Movimento virutale correttamente trasformato in quietanza."));
            PrimeFaces.current().executeScript("PF('trasformaDialog').hide();");
        }
        catch(IOException e) {
            logger.error("Impossibile elaborare il file {} a causa di {}", documento.getFileName(), e.toString());
            addMessage(Message.error("Impossibile elaborare il file per un errore inatteso: "+e.toString()));
        }
    }
    
    public QuietanzaRecord extractPDF(byte[] content) throws IOException {
        PDDocument doc = Loader.loadPDF(content);
        String txt = new PDFTextStripper().getText(doc);

        if(!txt.contains("Rendicontazione Movimentazioni") && !txt.contains("Alias RGS:")) return null;
        
        String[] lines = txt.split("\n");
        for(int i=0;i<lines.length;i++) {
            lines[i] = lines[i].replace("\n", "").replace("\r", "");
        }
        
        String causale = extract(lines, CAUSALE, String.class);
        String ordinante = extract(lines, ORDINANTE, String.class);
        LocalDate dataContabile = extract(lines, DATA_CONTABILE, LocalDate.class);
        BigDecimal importo = extract(lines, IMPORTO, BigDecimal.class);
        //String id = extract(lines, ID, String.class, true);
        String id = extract(lines, ID, String.class); // aggiornamento documenti PDF da febbraio 2025
        if(id.length()>16) {
            id = extract(lines, ID_LONG, String.class);
            if(id.length()>16) throw new IOException("Impossibile estrarre il numero ordinativo/quietanza. Verificare lo zoom del documento.");
        }

        QuietanzaRecord _quietanza = new QuietanzaRecord();
        _quietanza.setImporto(importo);
        //id = id.split("-")[3].trim()+"-"+id.split("-")[4].trim();
        _quietanza.setNumeroPagamento(id); 
        _quietanza.setDataPagamento(dataContabile);
        _quietanza.setOrdinante(ordinante);
        _quietanza.setDescrizioneOrdinanza(causale);
        if(notNull(ordinante).toUpperCase().startsWith("COM.STR.") || notNull(ordinante).toUpperCase().startsWith("COMMISSARIO")) {
            _quietanza.setFlag(1);
        }
        _quietanza.setVersione(1L);  
        return _quietanza;
    }
     
    public static <T> T extract(String[] lines, String[] startEnd, Class<T> type) {
        return extract(lines, startEnd, type, false);
    }
    
    public static <T> T extract(String[] lines, String[] startEnd, Class<T> type, boolean last) {
        List<String> lS = Arrays.asList(lines);
        int idxStart = last ? lS.lastIndexOf(startEnd[0]) : lS.indexOf(startEnd[0]);
        if (idxStart == -1) {
            return null;
        }
        int idxEnd = last ? lS.lastIndexOf(startEnd[1]) : lS.indexOf(startEnd[1]);
        if (idxEnd == -1) {
            idxEnd = lines.length;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = idxStart+1; i < idxEnd; i++) {
            sb.append(lines[i]).append(" ");
        }
        switch (type.getName()) {
            case "java.time.LocalDate" -> {
                return type.cast(LocalDate.parse(sb.toString().trim(), DATE_PATTERN_LONG));
            }
            case "java.math.BigDecimal" -> {
                String data = sb.toString().trim().replace("EUR", "").replace(".", "").replace(",", ".").trim();
                return type.cast(new BigDecimal(data));
            }
            case "java.lang.String" -> {
                return type.cast(sb.toString().trim());
            }
            case "java.lang.Integer" -> {
                return type.cast(Integer.valueOf(sb.toString().trim()));
            }
        }
        
        return null;
    }
    
    /*public void trasforma() {
        if (documento == null) {
            addMessage(Message.warn("Selezionare un file prima di importare!"));
            return;
        }

        try {
            PDFOrdinativo pdfO = pe.buildOrdinativo(documento.getContent());

            if (pdfO == null) {
                addMessage(Message.warn("File NON importato correttamente: Il documento non è un ordinativo GeoCos."));
                return;
            }

            OrdinativoAppoggioRecord oa = new OrdinativoAppoggioRecord();
            oa.setNumeroPagamento(String.valueOf(pdfO.getNumeroOrdine()));
            oa.setDataPagamento(toLocalDate(pdfO.getDataOrdine()));
            oa.setImporto(pdfO.getImporto());
            oa.setProprietario(getUtente().getUsername());
            oa.setFatturaNumero(pdfO.getNumeroFattura());
            oa.setFatturaData(toLocalDate(pdfO.getDataFattura()));
            if (pdfO.getImportoTotaleDocumento() != null) {
                BigDecimal iva = pdfO.getImportoTotaleDocumento().subtract(pdfO.getImporto());
                oa.setImportoIva(iva);
            }
            oa.setDataElaborazione(LocalDate.now());
            Documento doc = new Documento(documento.getFileName(), null, documento.getContent(), codServ.getMimeType(documento.getContentType()));

            switch (pdfO.getModalitaEstinzione()) {
                case "56": {
                    ContoCorrenteBancario est = (ContoCorrenteBancario) pdfO.getEstinzione();
                    String ben = !isEmpty(est.getNomeECognomeDelBeneficiario()) ? est.getNomeECognomeDelBeneficiario() : est.getRagioneSocialeDelBeneficiario();
                    oa.setBeneficiario(ben);
                    String desc = est.getDescrizioneCausale() != null ? est.getDescrizioneCausale().split(",")[0] : null;
                    oa.setDescrizioneRts(desc);
                    ImportaOrdinativiController.DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                    oa.setIdCodice(dd.idCodice);
                    oa.setIdTipoRts(dd.idTipoRts);
                    oa.setIdTipoDocumento(dd.idTipoDoc);
                    oa.setNumeroDocumento(dd.numero != null ? String.valueOf(dd.numero) : null);
                    oa.setDataDocumento(dd.data);
                    break;
                }
                case "77": {
                    RiversamentoSuTU est = (RiversamentoSuTU) pdfO.getEstinzione();
                    oa.setBeneficiario(est.getRagioneSocialeDelBeneficiario());
                    String desc = est.getDescrizioneCausale() != null ? est.getDescrizioneCausale().split(",")[0] : null;
                    oa.setDescrizioneRts(desc);
                    ImportaOrdinativiController.DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                    oa.setIdCodice(dd.idCodice);
                    oa.setIdTipoRts(dd.idTipoRts);
                    oa.setIdTipoDocumento(dd.idTipoDoc);
                    oa.setNumeroDocumento(dd.numero != null ? String.valueOf(dd.numero) : null);
                    oa.setDataDocumento(dd.data);
                    break;
                }
                case "71": {
                    RiversamentoSuErario est = (RiversamentoSuErario) pdfO.getEstinzione();
                    Integer capitolo = est.getCapitolo();
                    Integer articolo = est.getArticolo();
                    Integer capo = est.getCapo();
                    StringBuilder sb = new StringBuilder("BILANCIO CONTO ENTRATA");
                    if (capo != null) {
                        sb.append(" CAPO ").append(capo);
                    }
                    if (capitolo != null) {
                        sb.append(" CAPITOLO ").append(capitolo);
                    }
                    if (articolo != null) {
                        sb.append(" ARTICOLO ").append(articolo);
                    }
                    oa.setBeneficiario(sb.toString());
                    String desc = est.getDescrizioneCausale() != null ? est.getDescrizioneCausale().split(",")[0] : null;
                    oa.setDescrizioneRts(desc);
                    ImportaOrdinativiController.DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                    oa.setIdCodice(dd.idCodice);
                    oa.setIdTipoRts(dd.idTipoRts);
                    oa.setIdTipoDocumento(dd.idTipoDoc);
                    oa.setNumeroDocumento(dd.numero != null ? String.valueOf(dd.numero) : null);
                    oa.setDataDocumento(dd.data);
                    break;
                }
                case "81": {
                    RegolazionePagamentiInContoSospeso est = (RegolazionePagamentiInContoSospeso) pdfO.getEstinzione();
                    oa.setBeneficiario(est.getRagioneSocialeDelBeneficiario());
                    String desc = est.getDescrizioneCausale() != null ? est.getDescrizioneCausale().split(",")[0] : null;
                    oa.setDescrizioneRts(desc);
                    ImportaOrdinativiController.DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                    oa.setIdCodice(dd.idCodice);
                    oa.setIdTipoRts(dd.idTipoRts);
                    oa.setIdTipoDocumento(dd.idTipoDoc);
                    oa.setNumeroDocumento(dd.numero != null ? String.valueOf(dd.numero) : null);
                    oa.setDataDocumento(dd.data);
                    break;
                }
                case "76": {
                    RiversamentoSuCS est = (RiversamentoSuCS) pdfO.getEstinzione();
                    oa.setBeneficiario(est.getRagioneSocialeDelBeneficiario());
                    String desc = est.getDescrizioneCausale() != null ? est.getDescrizioneCausale().split(",")[0] : null;
                    oa.setDescrizioneRts(desc);
                    ImportaOrdinativiController.DatiDocumento dd = getDatiDocumento(est.getDescrizioneCausale());
                    oa.setIdCodice(dd.idCodice);
                    oa.setIdTipoRts(dd.idTipoRts);
                    oa.setIdTipoDocumento(dd.idTipoDoc);
                    oa.setNumeroDocumento(dd.numero != null ? String.valueOf(dd.numero) : null);
                    oa.setDataDocumento(dd.data);
                    break;
                }
            }

            if (!oa.getImporto().equals(movimento.getImporto())) {
                addMessage(Message.warn("L'importo dell'ordinativo che si è importato differisce da quello del movimento virtuale!"));
            }

            CapitoloCompetenza cc = mCampComp.get(movimento.getIdCompetenza());
            int annoCorrente = LocalDate.now().getYear();
            if (cc.getAnno() <= annoCorrente) {
                oa.setIdCompetenza(movimento.getIdCompetenza());
            }
            oa.setNote(movimento.getNote());
            oas.trasformaMovimento(oa, Collections.singletonList(doc), movimento);

            aggiornaMovimenti();
            addMessage(Message.info("File importato correttamente."));
            PrimeFaces.current().executeScript("PF('trasformaDialog').hide()");
        } catch (ImportException ie) {
            addMessage(Message.error("File " + documento.getFileName() + ", errore: " + ie));
            logger.error("Importazione del file [{}] non riuscita a causa di {}", documento.getFileName(), ie.getMessage());
        } catch (Exception e) {
            addMessage(Message.error("File " + documento.getFileName() + ", errore: " + e));
            logger.error("Importazione del file [{}] non riuscita a causa di un errore inaspettato {}", documento.getFileName(), getStackTrace(e));
        }
    }*/

    public CodiceRecord getCodiceDescrizione(String descrCausale) {
        if (descrCausale != null && descrCausale.contains(",")) {
            String[] parts = descrCausale.split("\\,");
            String cod = parts[parts.length - 1].trim().replace(".", "").replace(" ", "");
            return codServ.getCodiceByCodiceComposto(cod);
        }

        return null;
    }

    /**
     *
     * @param descrCausale nel formato DE{C|T}NNNN-GGMMAA<blank>...
     * @return
     */
    private ImportaOrdinativiController.DatiDocumento getDatiDocumento(String descrCausale) {
        ImportaOrdinativiController.DatiDocumento dd = new ImportaOrdinativiController.DatiDocumento();
        if (descrCausale != null) {
            String[] parts = descrCausale.split(" ");
            if (parts.length > 1) {
                parts = parts[0].split("\\-");
                if (parts.length >= 2) {
                    if (parts[0].length() == 7) {
                        TipoDocumentoRecord tdr = codServ.getTipoDocumentoByDescr(parts[0].substring(0, 3).toUpperCase());
                        dd.idTipoDoc = (tdr != null) ? tdr.getId() : null;
                        try {
                            dd.numero = Integer.valueOf(parts[0].substring(3, 7));
                        } catch (NumberFormatException nfe) {
                        }

                        try {
                            dd.data = LocalDate.parse(parts[1], DateTimeFormatter.ofPattern("ddMMyy"));
                        } catch (DateTimeParseException dtpe) {
                        }
                    }
                }
            }

            parts = descrCausale.split("\\,");
            String cod = parts[parts.length - 1].trim().replace(".", "").replace(" ", "");
            TipoRtsRecord trr = codServ.getTipoRtsByCodice(cod.substring(0, 1).toUpperCase());
            dd.idTipoRts = (trr != null) ? trr.getId() : null;

            CodiceRecord cr = codServ.getCodiceByCodiceComposto(cod.substring(1));
            dd.idCodice = (cr != null) ? cr.getId() : null;
        }

        return dd;
    }
}
