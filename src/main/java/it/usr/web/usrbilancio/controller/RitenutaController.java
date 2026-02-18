/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.AnagraficaRecord;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoRecord;
import it.usr.web.usrbilancio.domain.tables.records.RitenutaRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.AnagraficaService;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.OrdinativoService;
import it.usr.web.usrbilancio.service.PDFExtractor;
import it.usr.web.usrbilancio.service.StaleRecordException;
import jakarta.ejb.EJBException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.util.LangUtils;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class RitenutaController extends BaseController {

    private final static String[] DATA_CONTABILE = {"Data contabile:", "Data di riferimento:"};
    private final static String[] IMPORTO = {"Importo:", "Data contabile:"};
    private final static String[] TIPOLOGIA = {"Tipologia:", "Ordinante:"};
    private final static String[] N_ORDINATIVO = {"Numero quietanza:", "Indicatore fruttifero"};
    private final static String[] N_ORDINATIVO_LONG = {"Numero quietanza:", "Indicatore fruttifero infruttifero:"};
    private final static String[] END2ENDID = {"End2EndID:", "IBAN imputazione provvisoria:"};
    private final static String[] MESI = {"GENNAIO", "FEBBRAIO", "MARZO", "APRILE", "MAGGIO", "GIUGNO", "LUGLIO", "AGOSTO", "SETTEMBRE", "OTTOBRE", "NOVEMBRE", "DICEMBRE"};
    private final static int TUTTE_LE_COMPETENZE = -1;
    private final static int FROM = 0;
    private final static int TO = 1;
    private final static int CU_FLAG = 1;
    private final static int RITENUTA = 0;
    private final static int IRAP = 1;
    private final static int RITENUTA_IRAP = 2;
    private final static BigDecimal PERCENTO = new BigDecimal(100);
    @Inject
    OrdinativoService os;
    @Inject
    CompetenzaService cs;
    @Inject
    CodiceService codServ;
    @Inject
    PDFExtractor pe;
    @Inject
    AnagraficaService as;
    @Inject
    @AppLogger
    Logger logger;
    Map<Integer, CapitoloCompetenza> mCampComp;
    Map<Integer, CodiceRecord> codici;
    Map<Integer, TipoRtsRecord> tipiRts;
    List<TipoRtsRecord> tipiRtsList;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    List<OrdinativoRecord> ordinativiRitenuta;
    List<OrdinativoRecord> ordinativiRitenutaSelezionati;
    List<OrdinativoRecord> ordinativiRitenutaFiltrati;
    List<OrdinativoRecord> ordinativiAggancio;
    OrdinativoRecord ordinativoAggancioSelezionato;
    OrdinativoRecord ordinativoSelezionato;
    AnagraficaRecord anagraficaSelezionata;
    RitenutaRecord ritenuta;
    List<AnagraficaRecord> anagrafica;
    TipoRtsRecord rtsIva;
    List<FilterMeta> filterBy;
    List<LocalDate> periodo;       
    BigDecimal parzialeRitenuta;
    BigDecimal parzialeImponibile;
    BigDecimal imponibileSelezione;
    BigDecimal totaleRitenuta;
    BigDecimal parzialeSelezione;
    BigDecimal importoRitenuta;
    BigDecimal importoIRAP;
    int mostra;
    int tipoRitenuta; // 0 = ritenuta, 1 = IRAP
    UploadedFile ordinativoCaricato;

    public void init() {
        codici = codServ.getCodiciAsMap();
        tipiRtsList = codServ.getTipiRts(CodiceService.GruppoRts.RTS_ORDINATIVO);
        tipiRts = new HashMap<>();
        tipiRtsList.forEach(t -> {
            tipiRts.put(t.getId(), t);
            if ("I".equalsIgnoreCase(t.getCodice()) && rtsIva == null) {
                rtsIva = t;
            }
        });
        tipiDocumento = codServ.getTipiDocumentoAsMap();
        mCampComp = new HashMap<>();
        cs.getCapitoliCompetenze().forEach(cc -> {
            mCampComp.put(cc.getId(), cc);
        });

        ordinativoSelezionato = null;
        ordinativoCaricato = null;
        ordinativiAggancio = null;
        ordinativoAggancioSelezionato = null;
        mostra = 0;

        filterBy = new ArrayList<>();
        impostaUltimoMese();
    }

    public List<OrdinativoRecord> getOrdinativiRitenuta() {
        return ordinativiRitenuta;
    }

    public void setOrdinativiRitenuta(List<OrdinativoRecord> ordinativiRitenuta) {
        this.ordinativiRitenuta = ordinativiRitenuta;
    }

    public List<LocalDate> getPeriodo() {
        return periodo;
    }

    public void setPeriodo(List<LocalDate> periodo) {
        this.periodo = periodo;
    }

    public List<OrdinativoRecord> getOrdinativiRitenutaSelezionati() {
        return ordinativiRitenutaSelezionati;
    }

    public void setOrdinativiRitenutaSelezionati(List<OrdinativoRecord> ordinativiRitenutaSelezionati) {
        this.ordinativiRitenutaSelezionati = ordinativiRitenutaSelezionati;
    }

    public List<OrdinativoRecord> getOrdinativiRitenutaFiltrati() {
        return ordinativiRitenutaFiltrati;
    }

    public void setOrdinativiRitenutaFiltrati(List<OrdinativoRecord> ordinativiRitenutaFiltrati) {
        this.ordinativiRitenutaFiltrati = ordinativiRitenutaFiltrati;
    }

    public BigDecimal getTotaleRitenuta() {
        return totaleRitenuta;
    }

    public void setTotaleRitenuta(BigDecimal totaleRitenuta) {
        this.totaleRitenuta = totaleRitenuta;
    }

    public BigDecimal getParzialeRitenuta() {
        return parzialeRitenuta;
    }

    public void setParzialeRitenuta(BigDecimal parzialeRitenuta) {
        this.parzialeRitenuta = parzialeRitenuta;
    }

    public List<OrdinativoRecord> getOrdinativiAggancio() {
        return ordinativiAggancio;
    }

    public void setOrdinativiAggancio(List<OrdinativoRecord> ordinativiAggancio) {
        this.ordinativiAggancio = ordinativiAggancio;
    }

    public OrdinativoRecord getOrdinativoAggancioSelezionato() {
        return ordinativoAggancioSelezionato;
    }

    public void setOrdinativoAggancioSelezionato(OrdinativoRecord ordinativoAggancioSelezionato) {
        this.ordinativoAggancioSelezionato = ordinativoAggancioSelezionato;
    }

    public OrdinativoRecord getOrdinativoSelezionato() {
        return ordinativoSelezionato;
    }

    public void setOrdinativoSelezionato(OrdinativoRecord ordinativoSelezionato) {
        this.ordinativoSelezionato = ordinativoSelezionato;
    }

    public CodiceRecord decodeCodice(int idCodice) {
        return codici.get(idCodice);
    }

    public List<TipoRtsRecord> getTipoRtsList() {
        return tipiRtsList;
    }

    public TipoRtsRecord decodeTipoRts(int idTipoRts) {
        return tipiRts.get(idTipoRts);
    }

    public TipoDocumentoRecord decodeTipoDocumento(int idTipoDocumento) {
        return tipiDocumento.get(idTipoDocumento);
    }

    public CapitoloCompetenza decodeCapitoloCompetenza(int idCapComp) {
        return mCampComp.get(idCapComp);
    }

    public void onPeriodoSelezionato(SelectEvent<LocalDate> event) {
        LocalDate end = periodo.get(1);
        periodo.set(1, end.plusMonths(1).minusDays(1));

        aggiornaOrdinativiRitenuta();
    }

    public int getMostra() {
        return mostra;
    }

    public void setMostra(int mostra) {
        this.mostra = mostra;
    }

    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }

    public BigDecimal getParzialeImponibile() {
        return parzialeImponibile;
    }

    public BigDecimal getImponibileSelezione() {
        return imponibileSelezione;
    }

    public List<AnagraficaRecord> getAnagrafica() {
        return anagrafica;
    }

    public AnagraficaRecord getAnagraficaSelezionata() {
        return anagraficaSelezionata;
    }

    public void setAnagraficaSelezionata(AnagraficaRecord anagraficaSelezionata) {
        this.anagraficaSelezionata = anagraficaSelezionata;
    }

    public RitenutaRecord getRitenuta() {
        return ritenuta;
    }

    public int getTipoRitenuta() {
        return tipoRitenuta;
    }

    public void setTipoRitenuta(int tipoRitenuta) {
        this.tipoRitenuta = tipoRitenuta;
    }

    public BigDecimal getImportoRitenuta() {
        return importoRitenuta;
    }

    public BigDecimal getImportoIRAP() {
        return importoIRAP;
    }
      
    public void aggiornaOrdinativiRitenuta() {
        if (mostra == -1) {
            List<OrdinativoRecord> lO = os.getOrdinativiRitenutaPeriodo(TUTTE_LE_COMPETENZE, periodo.get(FROM), periodo.get(TO), 0);
            ordinativiRitenuta = new ArrayList<>();
            lO.forEach(o -> {
                if (isBitSet(o.getFlag(), 1)) {
                    ordinativiRitenuta.add(o);
                }
            });
        } else {
            ordinativiRitenuta = os.getOrdinativiRitenutaPeriodo(TUTTE_LE_COMPETENZE, periodo.get(FROM), periodo.get(TO), mostra);
        }
        totaleRitenuta = BigDecimal.ZERO;
        parzialeImponibile = BigDecimal.ZERO;
        ordinativiRitenuta.forEach(oi -> {
            totaleRitenuta = totaleRitenuta.add(oi.getImportoRitenuta());
            parzialeImponibile = parzialeImponibile.add(oi.getImporto());
        });
        parzialeRitenuta = totaleRitenuta;

        aggiornaParzialeSelezione();

        ordinativiRitenutaSelezionati = null;
        ordinativiRitenutaFiltrati = null;

        anagraficaSelezionata = null;
        anagrafica = null;
    }

    public void aggiornaParzialeSelezione() {
        parzialeSelezione = BigDecimal.ZERO;
        imponibileSelezione = BigDecimal.ZERO;
        if (!isEmpty(ordinativiRitenutaSelezionati)) {
            ordinativiRitenutaSelezionati.forEach(o -> {
                parzialeSelezione = parzialeSelezione.add(o.getImportoRitenuta());
                imponibileSelezione = imponibileSelezione.add(o.getImporto());
            });
        }

    }

    public BigDecimal getParzialeSelezione() {
        return parzialeSelezione;
    }

    public UploadedFile getOrdinativoCaricato() {
        return ordinativoCaricato;
    }

    public void setOrdinativoCaricato(UploadedFile ordinativoCaricato) {
        this.ordinativoCaricato = ordinativoCaricato;
    }

    public boolean filtroGlobale(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (LangUtils.isBlank(filterText)) {
            return true;
        }

        OrdinativoRecord rec = (OrdinativoRecord) value;
        boolean match = contains(rec.getBeneficiario(), filterText)
                || contains(rec.getDescrizioneRts(), filterText);
        if (isDate(filterText)) {
            LocalDate d = toDate(filterText);
            match = match || d.equals(rec.getDataDocumento())
                    || d.equals(rec.getDataPagamento())
                    || d.equals(rec.getDataRicevimento())
                    || d.equals(rec.getFatturaData());
        }
        match = match || contains(rec.getFatturaNumero(), filterText);
        match = match || contains(rec.getNumeroDocumento(), filterText);
        match = match || contains(rec.getNumeroPagamento(), filterText);
        match = match || contains(toStringFormat(rec.getImporto()), filterText);
        match = match || contains(toStringFormat(rec.getImportoRitenuta()), filterText);

        return match;
    }

    public boolean filtroOrdinativoRitenuta(Object value, Object filter, Locale locale) {
        try {
            Integer f = Integer.valueOf((String) filter);
            switch (f) {
                case 0:
                    return (value == null); // NON PAGATA
                case 1:
                    return (value != null); // PAGATA
                case 2:                     // TUTTI/DEFAULT  
                default:
                    return true;
            }
        } catch (NumberFormatException nfe) {
            logger.error("Filtro per ordinativo ritenuta/pagata non valido: {}, valore: {}", filter, value);
            return true;
        }
    }

    public boolean filtroCU(Object value, Object filter, Locale locale) {
        try {
            Integer f = Integer.valueOf((String) filter);
            Integer v = (Integer) value;
            int cu = v & CU_FLAG;
            switch (f) {
                case 0:
                    return (cu == 0); // NON PAGATA
                case 1:
                    return (cu == CU_FLAG); // PAGATA
                case 2:               // TUTTI/DEFAULT  
                default:
                    return true;
            }
        } catch (NumberFormatException nfe) {
            logger.error("Filtro per ordinativo ritenuta/pagata non valido: {}, valore: {}", filter, value);
            return true;
        }
    }

    public boolean isCU(int flag) {
        return ((flag & CU_FLAG) == CU_FLAG);
    }

    public void aggancia() {
        if (isEmpty(ordinativiRitenutaSelezionati)) {
            addMessage(Message.warn("Selezionare almeno un ordinativo dalla tabella!"));
            return;
        }

        ordinativiAggancio = os.getOrdinativiPagamento(LocalDate.now());
        ordinativoAggancioSelezionato = null;

        //PrimeFaces.current().executeScript("PF('ordinativoTargetDialog').show();");
    }

    public void agganciaSingolo(OrdinativoRecord o) {
        ordinativiAggancio = os.getOrdinativiPagamento(o.getDataPagamento());
        ordinativoAggancioSelezionato = (o.getOrdinativoRitenuta() != null) ? os.getOrdinativoById(o.getOrdinativoRitenuta()) : null;
        ordinativiRitenutaSelezionati = new ArrayList<>();
        ordinativiRitenutaSelezionati.add(o);

        //PrimeFaces.current().executeScript("PF('ordinativoTargetDialog').show();");
    }

    public void aggiornaParziale() {
        parzialeRitenuta = BigDecimal.ZERO;
        parzialeImponibile = BigDecimal.ZERO;
        ordinativiRitenutaFiltrati.forEach(op -> {
            parzialeRitenuta = parzialeRitenuta.add(op.getImportoRitenuta());
            parzialeImponibile = parzialeImponibile.add(op.getImporto());
        });
    }

    public void impostaAnno() {
        periodo = new ArrayList<>(2);
        // determina primo e ultimo giorno del mese precedente a quello attuale
        LocalDate initial = LocalDate.now().withMonth(1);
        LocalDate start = initial.withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        periodo.add(start);
        periodo.add(end);

        aggiornaOrdinativiRitenuta();
    }

    public void impostaUltimoMese() {
        periodo = new ArrayList<>(2);
        // determina primo e ultimo giorno del mese precedente a quello attuale
        //LocalDate initial = LocalDate.now().minusMonths(1);
        LocalDate initial = LocalDate.now();
        LocalDate start = initial.withDayOfMonth(1);
        LocalDate end = initial.withDayOfMonth(initial.getMonth().length(initial.isLeapYear()));
        periodo.add(start);
        periodo.add(end);

        aggiornaOrdinativiRitenuta();
    }

    public void salvaAgganciati() {
        ordinativiRitenutaSelezionati.forEach(o -> {
            o.setOrdinativoRitenuta(ordinativoAggancioSelezionato.getId());
        });
        os.modifica(ordinativiRitenutaSelezionati);

        aggiornaOrdinativiRitenuta();
        PrimeFaces.current().executeScript("PF('ordinativoTargetDialog').hide();");
    }

    public void processaF24Caricato() {
        if (ordinativoCaricato == null) {
            addMessage(Message.warn("Selezionare un ordinativo!"));
            return;
        }

        try {
            Map<String, Object> f24Ritenuta = extractPDFRitenuta(ordinativoCaricato.getContent());

            logger.info("Elementi (n. {}) estratti dal file [{}] con dimensione [{}]: ", f24Ritenuta.size(), ordinativoCaricato.getFileName(), ordinativoCaricato.getSize());
            f24Ritenuta.forEach((k, v) -> {
                logger.info("\t[{}]=[{}]", k, v);
            });

            if (f24Ritenuta.size() < 3) {
                addMessage(Message.error("Il documento caricato non è un F24 Ritenuta"));
                return;
            }
            Documento doc = new Documento(ordinativoCaricato.getFileName(), null, ordinativoCaricato.getContent(), codServ.getMimeType(ordinativoCaricato.getContentType()));

            OrdinativoRecord o = new OrdinativoRecord();
            BigDecimal importo = (BigDecimal) f24Ritenuta.get("IMPORTO");
            o.setNumeroPagamento((String) f24Ritenuta.get("PROTOCOLLO"));
            o.setDataPagamento((LocalDate) f24Ritenuta.get("DATA"));
            o.setImporto(importo);

            // Se si preme il pulsante in basso ed è selezionato 1 solo ordinativo lo tratta come selezione singola
            if (ordinativiRitenutaSelezionati != null && ordinativiRitenutaSelezionati.size() == 1) {
                ordinativoSelezionato = ordinativiRitenutaSelezionati.get(0);
                logger.info("Dalla tabella RITENUTA è stato selezionato 1 solo record e verrà gestito come selezione singola.");
            }

            // singolo?
            if (ordinativoSelezionato != null) {
                if (!o.getImporto().equals(ordinativoSelezionato.getImportoRitenuta())) {
                    addMessage(Message.error("L'importo dell'ordinativo non è congruente con quello della ritenuta da pagare."));
                    return;
                }

                o.setIdTipoDocumento(ordinativoSelezionato.getIdTipoDocumento());
                o.setNumeroDocumento(ordinativoSelezionato.getNumeroDocumento());
                o.setDataDocumento(ordinativoSelezionato.getDataDocumento());
                o.setDataRicevimento(ordinativoSelezionato.getDataPagamento());
                o.setBeneficiario(ordinativoSelezionato.getBeneficiario() + " (RITENUTA)");
                o.setIdCodice(ordinativoSelezionato.getIdCodice());
                o.setIdCompetenza(ordinativoSelezionato.getIdCompetenza());
                o.setDescrizioneRts(ordinativoSelezionato.getDescrizioneRts());
                o.setIdTipoRts(rtsIva.getId());
                o.setFatturaNumero(ordinativoSelezionato.getFatturaNumero());
                o.setFatturaData(ordinativoSelezionato.getFatturaData());
                o.setConsolidamento((byte) 0);
                o.setRtsCompleto((byte) 0);
                o.setRtsStampato((byte) 0);
                o.setFlag(0);

                int flag = ordinativoSelezionato.getFlag();
                ordinativoSelezionato.setFlag(bitSet(flag, 1));

                os.inserisciRitenuta(o, Arrays.asList(ordinativoSelezionato), Arrays.asList(doc));
            } else {
                BigDecimal totale = BigDecimal.ZERO;
                for (OrdinativoRecord oi : ordinativiRitenutaSelezionati) {
                    totale = totale.add(oi.getImportoRitenuta());
                }

                if (!o.getImporto().equals(totale)) {
                    addMessage(Message.error("L'importo relativo alle voci ritenuta dell'F24 non è congruente con quello della ritenuta da pagare."));
                    return;
                }

                Map<OrdinativoRecord, OrdinativoRecord> oRit = new HashMap<>();
                ordinativiRitenutaSelezionati.forEach(oi -> {
                    OrdinativoRecord _o = o.copy();

                    _o.setIdTipoDocumento(oi.getIdTipoDocumento());
                    _o.setNumeroDocumento(oi.getNumeroDocumento());
                    _o.setDataDocumento(oi.getDataDocumento());
                    _o.setDataRicevimento(oi.getDataPagamento());
                    _o.setIdCodice(oi.getIdCodice());
                    _o.setIdCompetenza(oi.getIdCompetenza());
                    _o.setIdTipoRts(rtsIva.getId());
                    _o.setBeneficiario(oi.getBeneficiario() + " (RITENUTA)");
                    _o.setDescrizioneRts(oi.getDescrizioneRts());
                    _o.setFatturaNumero(oi.getFatturaNumero());
                    _o.setFatturaData(oi.getFatturaData());
                    _o.setConsolidamento((byte) 0);
                    _o.setRtsCompleto((byte) 0);
                    _o.setRtsStampato((byte) 0);
                    _o.setImporto(oi.getImportoRitenuta());
                    _o.setFlag(0);
                    _o.setVersione(0L);

                    int flag = oi.getFlag();
                    oi.setFlag(bitSet(flag, 1));

                    oRit.put(oi, _o);
                });

                os.inserisciRitenuta(oRit, doc, MESI[periodo.get(0).getMonthValue() - 1], periodo.get(0).getYear());
            }

            aggiornaOrdinativiRitenuta();
            addMessage(Message.info("F24 ritenuta caricato e agganciato correttamente."));
            PrimeFaces.current().executeScript("PF('caricaf24Dialog').hide();");
        } catch (IOException ie) {
            addMessage(Message.error("Errore di importazione: " + ie.getMessage()));
        } catch (Exception e) {
            addMessage(Message.error("Errore generale: " + e.toString()));
        }
    }

    public void processaOrdinativoCaricato() {
        try {
            OrdinativoRecord o = elaboraFile(ordinativoCaricato.getContent());
            if (o == null) {
                addMessage(Message.error("Il documento caricato non è un ordinativo Or.Te.S."));
                return;
            }
            Documento doc = new Documento(ordinativoCaricato.getFileName(), null, ordinativoCaricato.getContent(), codServ.getMimeType(ordinativoCaricato.getContentType()));

            int num = os.concludiIVARitenuta(o, doc);

            aggiornaOrdinativiRitenuta();
            addMessage(Message.info("Ordinativo Or.TeS. ritenuta caricato e concluso correttamente. Numero ordinativi aggiornati: " + num));
            PrimeFaces.current().executeScript("PF('caricaDialog').hide();");
        } catch (IOException ie) {
            addMessage(Message.error("Errore di importazione: " + ie.getMessage()));
            logger.error("Errore I/O di importazione RITENUTA: {}", ie);
        } catch (Exception e) {
            addMessage(Message.error("Errore generale: " + e.toString()));
            logger.error("Errore generale di importazione RITENUTA: {}", e);
        }
    }

    public Map<String, Object> extractPDFRitenuta(byte[] content) throws Exception {
        DecimalFormatSymbols ss = new DecimalFormatSymbols();
        ss.setGroupingSeparator('.');
        ss.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,##0.00", ss);
        df.setParseBigDecimal(true);

        PDDocument doc = Loader.loadPDF(content);
        String txt = new PDFTextStripper().getText(doc);

        Map<String, Object> result = new HashMap<>();
        String[] lines = txt.split("\n");
        Pattern pImp = Pattern.compile("Erario (10[456]E) \\d{4} \\d{4} ([0-9\\.]+ \\d{2})");
        Pattern pProt = Pattern.compile(".*stato protocollato con il numero ([0-9]+) in data (\\d{2}/\\d{2}/\\d{4})");
        BigDecimal _totaleRitenuta = BigDecimal.ZERO;
        for (String line : lines) {
            line = line.replaceAll("\\r|\\n", "");
            Matcher m = pImp.matcher(line);
            if (m.matches()) {
                String sImp = m.group(2).replace(" ", ",");
                BigDecimal imp = (BigDecimal) df.parse(sImp);
                _totaleRitenuta = _totaleRitenuta.add(imp);
            }
            m = pProt.matcher(line);
            if (m.find()) {
                result.put("PROTOCOLLO", m.group(1));
                result.put("DATA", LocalDate.parse(m.group(2), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        }

        if (!_totaleRitenuta.equals(BigDecimal.ZERO)) {
            result.put("IMPORTO", _totaleRitenuta);
        } else {
            result.clear();
        }

        return result;
    }

    private OrdinativoRecord elaboraFile(byte[] pdfFile) throws IOException {
        PDDocument doc = Loader.loadPDF(pdfFile);
        String txt = new PDFTextStripper().getText(doc);

        if (!txt.contains("Rendicontazione Movimentazioni") && !txt.contains("Alias RGS:")) {
            return null;
        }

        String[] lines = txt.split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].replace("\n", "").replace("\r", "");
        }

        String tipologia = extract(lines, TIPOLOGIA, String.class);
        if (!"045".equalsIgnoreCase(tipologia)) {
            throw new IOException("Tipologia documento NON corretta.");
        }
        LocalDate dataContabile = extract(lines, DATA_CONTABILE, LocalDate.class);
        BigDecimal importo = extract(lines, IMPORTO, BigDecimal.class);
        String numOrd = extract(lines, N_ORDINATIVO, String.class);
        if (numOrd.length() > 16) {
            numOrd = extract(lines, N_ORDINATIVO_LONG, String.class);
            if (numOrd.length() > 16) {
                throw new IOException("Impossibile estrarre il numero ordinativo/quietanza. Verificare lo zoom del documento.");
            }
        }
        String end2End = extract(lines, END2ENDID, String.class);
        if (isEmpty(end2End)) {
            throw new IOException("Impossibile estrarre il protocollo End2End. Verificare lo zoom del documento.");
        }
        String[] e2e = end2End.split("-");
        if (e2e.length != 2) {
            throw new IOException("Il numero End2End non è nel formato corretto <n>-<m>. Verificare lo zoom del documento.");
        }

        OrdinativoRecord or = new OrdinativoRecord();
        or.setDataPagamento(dataContabile);
        or.setNumeroPagamento(numOrd);
        or.setImporto(importo);
        or.setDescrizioneRts(e2e[0]);

        return or;
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
        for (int i = idxStart + 1; i < idxEnd; i++) {
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

    /**
     *
     * @param descrCausale nel formato DE{C|T}NNNN-GGMMAA<blank>...
     * @return
     */
    private ImportaOrtesController.DatiDocumento getDatiDocumento(String descrCausale) {
        ImportaOrtesController.DatiDocumento dd = new ImportaOrtesController.DatiDocumento();
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

    public String rowColor(OrdinativoRecord o) {
        if (o != null) {
            if (isBitSet(o.getFlag(), 1)) {
                return "lightcyan";
            }
            if (o.getOrdinativoRitenuta() != null) {
                return "lightgreen";
            }
        }

        return "";
    }

    public void aggiornaAnagrafica() {
        anagraficaSelezionata = as.getAnagraficaOrdinativo(ordinativoSelezionato);
        anagrafica = as.getAnagrafica();
        if (anagraficaSelezionata != null) {
            ritenuta = as.getRitenuta(ordinativoSelezionato.getId(), anagraficaSelezionata.getId());
            if(ritenuta.getPercRitenuteOperate()!=null && ritenuta.getPercIrapVersata()!=null) {
                tipoRitenuta = RITENUTA_IRAP;
            }
            else {
                tipoRitenuta = ritenuta.getPercRitenuteOperate() != null ? RITENUTA : IRAP;
            }            
        } else {
            ritenuta = new RitenutaRecord();                        
            tipoRitenuta = RITENUTA;
        }
        
        // Reimposta ai valori di default se assente
        // il valore non necessario verrà annullato in fase di salvataggio
        if(ritenuta.getPercRitenuteOperate()==null) ritenuta.setPercRitenuteOperate(new BigDecimal(20));
        if(ritenuta.getPercIrapVersata()==null) ritenuta.setPercIrapVersata(new BigDecimal(8.5));

        ricalcolaImporti();
    }

    public void ricalcolaImporti() {
        if (ritenuta.getImponibile() != null) {
            importoRitenuta = ritenuta.getPercRitenuteOperate() != null ? ritenuta.getImponibile().multiply(ritenuta.getPercRitenuteOperate()).divide(PERCENTO) : BigDecimal.ZERO;
            importoIRAP = ritenuta.getPercIrapVersata() != null ? ritenuta.getImponibile().multiply(ritenuta.getPercIrapVersata()).divide(PERCENTO) : BigDecimal.ZERO;
        } else {
            importoRitenuta = BigDecimal.ZERO;
            importoIRAP = BigDecimal.ZERO;
        }
    }

    public String formattaNominativo(AnagraficaRecord ar) {
        if (ar == null) {
            return null;
        }

        StringJoiner sj = new StringJoiner(" ");
        sj.add(ar.getCognomeRagsoc());
        sj.add(ar.getNome() != null ? ar.getNome() : "");
        return sj.toString().trim();
    }

    public void salvaAnagraficaRitenuta() {
        try {
            ritenuta.setIdAnagrafica(anagraficaSelezionata.getId());
            switch(tipoRitenuta) {
                case RITENUTA ->  {
                    ritenuta.setPercIrapVersata(null);
                }
                case IRAP ->  {
                    ritenuta.setPercRitenuteOperate(null);
                }
                case RITENUTA_IRAP -> {                    
                } 
            }
            
            if (ritenuta.getId() != null) {
                as.modifica(ritenuta);
            } else {
                ritenuta.setIdOrdinativo(ordinativoSelezionato.getId()); 
                as.inserisci(ritenuta);
            }

            addMessage(Message.info("Anagrafica e importi tributari aggiornati."));
            PrimeFaces.current().executeScript("PF('ritenuteDialog').hide();");
        } catch (EJBException ex) {
            if (ex.getCausedByException() instanceof StaleRecordException) {
                addMessage(Message.warn(ex.getCausedByException().getMessage()));
            } else {
                addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante il salvataggio dell'anagrafica-ritenuta {}. Errore: {}", ex.getCausedByException().getClass(), ritenuta, ex.getCausedByException());
            }
        }
    }
    
    public boolean anagraficaCollegata(OrdinativoRecord ord) {
        return as.getAnagraficaOrdinativo(ord)!=null; 
    }
}
 