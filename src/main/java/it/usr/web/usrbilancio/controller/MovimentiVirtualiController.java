/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.pdfextract.model.ContoCorrenteBancario;
import it.usr.pdfextract.model.PDFOrdinativo;
import it.usr.pdfextract.model.RegolazionePagamentiInContoSospeso;
import it.usr.pdfextract.model.RiversamentoSuCS;
import it.usr.pdfextract.model.RiversamentoSuErario;
import it.usr.pdfextract.model.RiversamentoSuTU;
import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.MovimentiVirtualiRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.DuplicationException;
import it.usr.web.usrbilancio.service.ImportException;
import it.usr.web.usrbilancio.service.MovimentiVirtualiService;
import it.usr.web.usrbilancio.service.OrdinativoAppoggioService;
import it.usr.web.usrbilancio.service.PDFExtractor;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
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
public class MovimentiVirtualiController extends BaseController {

    @Inject
    PDFExtractor pe;
    @Inject
    MovimentiVirtualiService mvs;
    @Inject
    CompetenzaService cs;
    @Inject
    CodiceService codServ;
    @Inject
    OrdinativoAppoggioService oas;
    @Inject
    @AppLogger
    Logger logger;
    CapitoloCompetenza capCompSelezionato;
    CapitoloCompetenza movimentoCapComp;
    CodiceRecord movimentoCodice;
    Integer movimentoCodiceFiltro;
    TipoDocumentoRecord movimentoTipoDocumento;
    List<CapitoloCompetenza> capComp;
    List<CapitoloCompetenza> capCompAperti;
    Map<Integer, CapitoloCompetenza> mCampComp;
    Map<Integer, CodiceRecord> codici;
    List<CodiceRecord> codiciList;
    Map<Integer, TipoDocumentoRecord> tipiDocumento;
    List<MovimentiVirtualiRecord> movimenti;
    List<MovimentiVirtualiRecord> movimentiFiltrati;
    MovimentiVirtualiRecord movimento;
    UploadedFile documento;
    BigDecimal totale;
    BigDecimal totaleParziale;
    String azione;

    public void init() {
        codiciList = codServ.getCodici();
        codici = new HashMap<>();
        codiciList.forEach(c -> codici.put(c.getId(), c));
        capComp = cs.getCapitoliCompetenze();
        capCompAperti = cs.getCapitoliCompetenzeAperti();
        mCampComp = new HashMap<>();
        capComp.forEach(cc -> mCampComp.put(cc.getId(), cc));
        tipiDocumento = codServ.getTipiDocumentoAsMap();

        capCompSelezionato = null;
        movimentoCapComp = null;
        movimentoCodice = null;
        movimentoCodiceFiltro = null;
        movimentoTipoDocumento = null;
        totale = null;
        totaleParziale = null;
        documento = null;
        azione = null;
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
            movimentoCodice = codici.get(movimento.getIdCodice());
            movimentoTipoDocumento = tipiDocumento.get(movimento.getIdTipoDocumento());
            azione = "Modifica";
        }
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

    public Map<Integer, CapitoloCompetenza> getmCampComp() {
        return mCampComp;
    }

    public void setmCampComp(Map<Integer, CapitoloCompetenza> mCampComp) {
        this.mCampComp = mCampComp;
    }

    public Map<Integer, CodiceRecord> getCodici() {
        return codici;
    }

    public List<CodiceRecord> getCodiciList() {
        return this.codiciList;
    }

    public void setCodici(Map<Integer, CodiceRecord> codici) {
        this.codici = codici;
    }

    public Map<Integer, TipoDocumentoRecord> getTipiDocumento() {
        return tipiDocumento;
    }

    public Collection<TipoDocumentoRecord> getTipiDocumentoList() {
        return tipiDocumento.values();
    }

    public void setTipiDocumento(Map<Integer, TipoDocumentoRecord> tipiDocumento) {
        this.tipiDocumento = tipiDocumento;
    }

    public Integer getMovimentoCodiceFiltro() {
        return movimentoCodiceFiltro;
    }

    public void setMovimentoCodiceFiltro(Integer movimentoCodiceFiltro) {
        this.movimentoCodiceFiltro = movimentoCodiceFiltro;
    }

    public CodiceRecord decodeCodice(int idCodice) {
        return codici.get(idCodice);
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

    public CapitoloCompetenza getCapCompSelezionato() {
        return capCompSelezionato;
    }

    public void setCapCompSelezionato(CapitoloCompetenza capCompSelezionato) {
        this.capCompSelezionato = capCompSelezionato;
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

    public String getAzione() {
        return azione;
    }

    public boolean isChiuso() {
        return (capCompSelezionato != null) ? (capCompSelezionato.getChiuso() == 1 || capCompSelezionato.getChiuso() == 2) : false;
    }

    public void aggiornaMovimenti() {
        movimenti = mvs.getMovimentiVirtuali(capCompSelezionato.getId(), -1);
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
        if (movimentiFiltrati != null) {
            movimentiFiltrati.forEach(mf -> {
                totaleParziale = totaleParziale.add(mf.getImporto());
            });
        }
    }

    public boolean filtroGlobale(Object value, Object filter, Locale locale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (LangUtils.isBlank(filterText)) {
            return true;
        }

        MovimentiVirtualiRecord rec = (MovimentiVirtualiRecord) value;
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

        movimentoCapComp = capCompSelezionato;
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

    public void duplica() {
        MovimentiVirtualiRecord mvr = movimento.copy();
        mvr.setId(null);
        mvr.setVersione(1L);
        mvr.setDescrizioneRagioneria("COPIA-"+movimento.getDescrizioneRagioneria());
        movimentoCapComp = mCampComp.get(mvr.getIdCompetenza());
        mvr.setIdCompetenza(null);
        movimento = mvr;
        azione = "Duplica";
    }

    public void trasforma() {
        if (documento == null) {
            addMessage(Message.warn("Selezionare uno o più file prima di importare!"));
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
    }

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
