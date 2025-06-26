/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.BaseController;
import it.usr.web.producer.AppLogger;
import it.usr.web.usrbilancio.domain.tables.records.RichiestaRecord;
import it.usr.web.usrbilancio.model.CapitoloCompetenza;
import it.usr.web.usrbilancio.model.Documento;
import it.usr.web.usrbilancio.model.Situazione;
import it.usr.web.usrbilancio.service.CompetenzaService;
import it.usr.web.usrbilancio.service.IntegrityException;
import it.usr.web.usrbilancio.service.MovimentiVirtualiService;
import it.usr.web.usrbilancio.service.OrdinativoService;
import it.usr.web.usrbilancio.service.QuietanzaService;
import it.usr.web.usrbilancio.service.RichiestaService;
import jakarta.ejb.EJBException;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.map.HashedMap;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ViewScoped
public class SituazioneController extends BaseController {

    @Inject
    CompetenzaService cs;
    @Inject
    OrdinativoService os;
    @Inject
    QuietanzaService qs;
    @Inject
    RichiestaService rs;
    @Inject
    MovimentiVirtualiService mvs;
    @Inject
    @AppLogger
    Logger logger;
    boolean annoCorrente;
    List<Situazione> situazione;
    TreeNode<Situazione> situazioneTree;
    TreeNode<Situazione> nodoSelezionato;
    Situazione selezione;
    List<RichiestaRecord> richieste;
    RichiestaRecord richiesta;
    UploadedFile documento;

    public void init() {
        annoCorrente = true;

        aggiornaSituazione();
    }

    public TreeNode<Situazione> getNodoSelezionato() {
        return nodoSelezionato;
    }

    public void setNodoSelezionato(TreeNode<Situazione> nodoSelezionato) {
        this.nodoSelezionato = nodoSelezionato;
    }
        
    public List<Situazione> getSituazione() {
        return situazione;
    }

    public boolean isAnnoCorrente() {
        return annoCorrente;
    }

    public void setAnnoCorrente(boolean annoCorrente) {
        this.annoCorrente = annoCorrente;
    }

    public TreeNode<Situazione> getSituazioneTree() {
        return situazioneTree;
    }

    public Situazione getSelezione() {
        return selezione;
    }

    public RichiestaRecord getRichiesta() {
        return richiesta;
    }

    public void setRichiesta(RichiestaRecord richiesta) {
        this.richiesta = richiesta;
    }

    public void setSelezione(Situazione selezione) {
        this.selezione = selezione;

        aggiornaRichieste();
    }

    public List<RichiestaRecord> getRichieste() {
        return richieste;
    }

    public UploadedFile getDocumento() {
        return documento;
    }

    public void setDocumento(UploadedFile documento) {
        this.documento = documento;
    }
        
    public BigDecimal sum(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            a = BigDecimal.ZERO;
        }
        if (b == null) {
            b = BigDecimal.ZERO;
        }

        return a.add(b);
    }

    public BigDecimal sub(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return null;
        }
        if (a == null) {
            a = BigDecimal.ZERO;
        }
        if (b == null) {
            b = BigDecimal.ZERO;
        }

        return a.subtract(b);
    }

    public String getFormat(BigDecimal i) {
        if (i != null) {
            int res = i.compareTo(BigDecimal.ZERO);
            return res < 0 
                    ? "red"
                    : "green";
        }

        return "";
    }

    public void aggiornaSituazione() {
        List<CapitoloCompetenza> capComp = cs.getCapitoliCompetenzeConStanziamento(annoCorrente ? getAnnoAttuale() : null);
        situazione = new ArrayList<>(capComp.size());
        capComp.forEach((var cc) -> {
            Situazione s = new Situazione();
            s.setIdCompetenza(cc.getId());
            s.setCapitolo(cc.getDescrizione());
            s.setAnno(cc.getAnno());
            s.setChiuso(Integer.valueOf(cc.getChiuso()));

            s.setStanziamento(cc.getStanziamento());
            s.setSpeso(os.getTotaleOrdinativi(s.getIdCompetenza()));
            s.setFinanziato(qs.getTotaleQuietanze(s.getIdCompetenza()));
            s.setPreventivato(mvs.getTotaleMovimenti(s.getIdCompetenza()));
            s.setRichiesto(rs.getTotaleRichieste(s.getIdCompetenza()));

            situazione.add(s);
        });

        Map<String, List<Situazione>> ccMap = new HashedMap<>();
        situazione.forEach(s -> {
            if (ccMap.get(s.getCapitolo()) == null) {
                ccMap.put(s.getCapitolo(), new ArrayList());
            }

            ccMap.get(s.getCapitolo()).add(s);
        });

        situazioneTree = new DefaultTreeNode<>();
        ccMap.forEach((descr, ss) -> {
            if (!ss.isEmpty()) {
                Situazione top = new Situazione();
                top.setIdCompetenza(ss.get(0).getIdCompetenza());
                top.setCapitolo(ss.get(0).getCapitolo());
                top.setChiuso(null);
                //top.setAnno(ss.get(0).getAnno());
 
                TreeNode<Situazione> cap = new DefaultTreeNode<>(top, situazioneTree);
                BigDecimal stanziato = BigDecimal.ZERO;
                BigDecimal speso = BigDecimal.ZERO;
                BigDecimal finanziato = BigDecimal.ZERO;
                BigDecimal preventivato = BigDecimal.ZERO;
                BigDecimal richiesto = BigDecimal.ZERO;
                for (Situazione s : ss) {
                    s.setCapitolo(null);
                    TreeNode<Situazione> x = new DefaultTreeNode<>(s, cap);

                    stanziato = stanziato.add(s.getStanziamento() != null ? s.getStanziamento() : BigDecimal.ZERO);
                    speso = speso.add(s.getSpeso() != null ? s.getSpeso() : BigDecimal.ZERO);
                    finanziato = finanziato.add(s.getFinanziato() != null ? s.getFinanziato() : BigDecimal.ZERO);
                    preventivato = preventivato.add(s.getPreventivato() != null ? s.getPreventivato() : BigDecimal.ZERO);
                    richiesto = richiesto.add(s.getRichiesto() != null ? s.getRichiesto() : BigDecimal.ZERO);
                }

                /*top.setStanziamento(stanziato);
                top.setSpeso(speso);
                top.setFinanziato(finanziato);
                top.setPreventivato(preventivato);
                top.setRichiesto(richiesto);*/
                cap.setExpanded(true);
            }
        });

        setSelezione(null);
        nodoSelezionato = null;
    }

    public void aggiornaRichieste() {
        richieste = (selezione != null) ? cs.getRichieste(selezione.getIdCompetenza()) : new ArrayList();
        richiesta = new RichiestaRecord();
    }
    
    public void nuovaRichiesta() {
        documento = null;
        richiesta = new RichiestaRecord();        
    }
    
    public void eliminaRichiesta(RichiestaRecord r) {
        try {
            cs.elimina(r);
            
            aggiornaRichieste();
            addMessage(Message.info("Richiesta eliminata correttamente!"));
        } catch (EJBException ex) {
            if(ex.getCausedByException() instanceof IntegrityException) {
                addMessage(Message.error(ex.getCausedByException().getMessage()));
            }
            else {
                addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'eliminazione dela richiesta {}. Errrore: {}", ex.getCausedByException().getClass(), r, ex.getCausedByException());
            }
        }
    }
    
    public void salvaRichiesta() {
        try {
            richiesta.setIdCompetenza(selezione.getIdCompetenza());
            richiesta.setVersione(1L);
            
            Documento d = new Documento(documento.getFileName(), null, documento.getContent(), null);
            cs.salva(richiesta, d);
            
            aggiornaRichieste();
            PrimeFaces.current().executeScript("PF('richiestaDialog').hide();");
            addMessage(Message.info("Richiesta aggiunta correttamente!"));
        } catch (EJBException ex) {
            if(ex.getCausedByException() instanceof IntegrityException) {
                addMessage(Message.error(ex.getCausedByException().getMessage()));
            }
            else {
                addMessage(Message.error("Errore imprevisto: " + ex.getCausedByException().getMessage()));
                logger.debug("Errore imprevisto {} durante l'aggiunta dela richiesta {}. Errrore: {}", ex.getCausedByException().getClass(), richiesta, ex.getCausedByException());
            }
        }
    }
}
