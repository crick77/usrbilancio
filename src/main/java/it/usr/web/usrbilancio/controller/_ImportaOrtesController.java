/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import it.usr.web.usrbilancio.domain.tables.records.OrdinativoAppoggioRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoDocumentoRecord;
import it.usr.web.usrbilancio.domain.tables.records.TipoRtsRecord;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.service.CodiceService;
import it.usr.web.usrbilancio.service.OrdinativoAppoggioService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.model.file.UploadedFiles;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class _ImportaOrtesController extends BaseController {

    @Inject
    OrdinativoAppoggioService oas;
    @Inject
    CodiceService codServ;
    @Inject
    @AppLogger
    Logger logger;
    List<ImportaOrdinativiController.Result> result;
    UploadedFiles documenti;

    public void init() {
        result = null;
        documenti = null;
    }

    public UploadedFiles getDocumenti() {
        return documenti;
    }

    public void setDocumenti(UploadedFiles documenti) {
        this.documenti = documenti;
    }

    public List<ImportaOrdinativiController.Result> getResult() {
        return result;
    }

    public void elabora() {
        result = null;

        if (documenti == null || isEmpty(documenti.getFiles())) {
            addMessage(Message.warn("Selezionare uno o più file prima di importare!"));
            return;
        }

        if (!documenti.getFiles().stream().anyMatch(d -> FileNameUtils.getExtension(Paths.get(d.getFileName())).equalsIgnoreCase("xlsx"))) {
            addMessage(Message.warn("Tra i file caricati non è presente un XLSX."));
            return;
        }

        result = new ArrayList<>();
        AtomicInteger processati = new AtomicInteger(0);
        AtomicInteger errori = new AtomicInteger(0);
        List<OrdinativoAppoggioRecord> ordinativi = new ArrayList<>();
        documenti.getFiles().forEach(d -> {
            if (FileNameUtils.getExtension(Paths.get(d.getFileName())).equalsIgnoreCase("xlsx")) {
                try {
                    processati.incrementAndGet();
                    List<OrdinativoAppoggioRecord> lElab = elaboraXLSX(d.getContent());
                    ordinativi.addAll(lElab);
                    ImportaOrdinativiController.Result r = new ImportaOrdinativiController.Result(false, d.getFileName(), "File importato correttamente.");
                    r.setNote("Ordinativi contenuti: "+lElab.size());
                    result.add(r);
                } catch (IOException | InvalidFormatException e) {
                    errori.incrementAndGet();
                    addMessage(Message.warn(e.getMessage()));
                }
            }
        });

        // processa gli ordinativi elaborati e associa il file PDF
        List<Documento> emptyD = new ArrayList<>(0);
        ordinativi.forEach(o -> {
            try {
                UploadedFile f = documenti.getFiles().stream().filter(d -> isOpiFile(d.getFileName(), o.getNumeroPagamento())).findFirst().get();                                                
                ImportaOrdinativiController.Result r;
                if (f != null) {
                    Documento doc = new Documento(f.getFileName(), null, f.getContent(), codServ.getMimeType(f.getContentType()));

                    oas.inserisci(o, Collections.singletonList(doc));                    
                    r = new ImportaOrdinativiController.Result(false, o.getNumeroPagamento(), "Documento [" + f.getFileName() + "] abbinato correttamente all'ordinativo.");
                } else {
                    oas.inserisci(o, emptyD);
                    r = new ImportaOrdinativiController.Result(false, o.getNumeroPagamento(), "Per l'ordinativo non è stato caricato il documento corrispondente. Procedere manualmente.");
                }

                result.add(r);
                processati.incrementAndGet();
            } catch (Exception e) {
                addMessage(Message.error("Ordinativo " + o.getNumeroPagamento()+ ", errore: " + e));
                logger.error("Importazione ordinativo [{}] non riuscita a causa di un errore inaspettato {}", o.getNumeroPagamento(), getStackTrace(e));
                ImportaOrdinativiController.Result r = new ImportaOrdinativiController.Result(true, o.getNumeroPagamento(), "Ordinativo non caricato in archivio.", e.toString());
                result.add(r);
                errori.incrementAndGet();
            }
        });

        if(errori.get()>0) {
            addMessage(Message.warn("Procedura di importazione completata con errori/notifiche. File totali: "+documenti.getFiles().size()+", File processati: "+processati+", errori/notifiche: "+errori));
        }
        else {
            addMessage(Message.info("Procedura di importazione completata con successo! File totali: "+documenti.getFiles().size()+", File processati: "+processati));
        }
    }

    public boolean isOpiFile(String fileName, String numeroPagamento) {
        if(isEmpty(fileName) || isEmpty(numeroPagamento)) return false;
        fileName = fileName.toUpperCase();
        numeroPagamento = numeroPagamento.toUpperCase();
        
        return fileName.startsWith("OPI "+numeroPagamento) && fileName.endsWith(".PDF");        
    }
    
    public List<OrdinativoAppoggioRecord> elaboraXLSX(byte[] xlsDoc) throws IOException, InvalidFormatException {
        List<OrdinativoAppoggioRecord> lOrd = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsDoc))) {
            XSSFSheet sheet = workbook.getSheet("SAP Document Export");
            if (sheet == null) {
                throw new InvalidFormatException("Il documento XLSX non è un documento Or.Te.S.");
            }

            for (int rowNum = 1;; rowNum++) {
                XSSFRow row = sheet.getRow(rowNum);
                if (row == null) {
                    break;
                }
                String numeroPagamento = row.getCell(0).getStringCellValue();
                LocalDateTime dataPagamento = row.getCell(1).getLocalDateTimeCellValue();
                String beneficiario = row.getCell(2).getStringCellValue();
                String causale = row.getCell(6).getStringCellValue();
                BigDecimal importo = new BigDecimal(row.getCell(8).getNumericCellValue());

                OrdinativoAppoggioRecord o = new OrdinativoAppoggioRecord();
                o.setNumeroPagamento(numeroPagamento);
                o.setDataPagamento(dataPagamento.toLocalDate());
                o.setImporto(importo);
                o.setProprietario(getUtente().getUsername());
                o.setBeneficiario(beneficiario);
                ImportaOrdinativiController.DatiDocumento dd = getDatiDocumento(causale);
                o.setDescrizioneRts(dd.causale);
                o.setIdCodice(dd.idCodice);
                o.setIdTipoRts(dd.idTipoRts);
                o.setIdTipoDocumento(dd.idTipoDoc);
                o.setNumeroDocumento(dd.numero != null ? String.valueOf(dd.numero) : null);
                o.setDataDocumento(dd.data);
                o.setDataElaborazione(now);

                lOrd.add(o);
            }

            return lOrd;
        }
    }

    public boolean allErrors(List<ImportaOrdinativiController.Result> res) {
        return (res==null) ? true : res.stream().allMatch(el -> el.isError());
    }
    
    public String vediImportati() {        
        return Redirector.toPath("importati").withParam("d", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).withRedirect().go();                
    }
    
    /**
     *
     * @param descrCausale nel formato
     * DE{C|T}NNNN-GGMMAA<blank>...,<RTS><CODICE>
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

            int space = descrCausale.indexOf(" ");
            int comma = descrCausale.indexOf(",");
            if (comma == -1) {
                comma = descrCausale.length();
            }
            dd.causale = descrCausale.substring(space+1, comma);

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
