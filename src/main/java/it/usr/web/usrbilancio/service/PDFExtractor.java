/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import it.usr.pdfextract.BeanFiller;
import it.usr.pdfextract.ModalitaEstinzione;
import it.usr.pdfextract.QuietanzaToken;
import it.usr.pdfextract.Token;
import it.usr.pdfextract.model.QuietanzaGeocos;
import it.usr.pdfextract.model.Estinzione;
import it.usr.pdfextract.model.PDFOrdinativo;
import it.usr.web.producer.AppLogger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;

/** 
 *
 * @author riccardo.iovenitti
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)        
public class PDFExtractor {
    @Inject
    @AppLogger
    Logger logger;
    public final static Token[] TOKENS = { new Token("Numero Conto / Sezione di Tesoreria:", "/", 2),
                                           new Token("Data Ordine:"), 
                                           new Token("Numero Ordine:"), 
                                           new Token("Anno Emissione:"), 
                                           new Token("Stato Titolo:"), 
                                           new Token("Importo: €"), 
                                           new Token("Importo in lettere:"), 
                                           new Token("Modalità estinzione:", "-", 2), 
                                           new Token("Provenienza fondi:"), 
                                           new Token("Capitolo / Ragioneria / Previsione / Appendice / Articolo:", "/", 5), 
                                           new Token("Codice Amministrazione Autonoma:"),
                                           new Token("Sezione Provenienza Fondi:"),
                                           new Token("Conto CS provenienza Fondi:"),
                                           new Token("Conto TU provenienza Fondi:"),
                                           new Token("Codice ministero FD:"),
                                           new Token("Codice qualifica FD:"),
                                           new Token("Progressivo FD:"),
                                           new Token("Tesoreria FD:"),
                                           new Token("Dettagli provenienza fondi:"),
                                           new Token("CIG:"),
                                           new Token("CUP:"),
                                           new Token("CPV:"),
                                           new Token("Data Esigibilità:"),
                                           new Token("Codice Gestionale: ", "-", 2),
                                           new Token("Numero Fattura:"),
                                           new Token("Data Fattura:"),
                                           new Token("Importo totale documento: € ")
                                        };
    public final static QuietanzaToken[] QTOKENS = { new QuietanzaToken("Data contabile"),
                                                     new QuietanzaToken("Numero quietanza"),
                                                     new QuietanzaToken("Importo"),
                                                     new QuietanzaToken("Versante", 255),
                                                     new QuietanzaToken("Causale", "Codice amm. aut.", 255)
                                                   };        
    private final static char[] LINE_SEP = System.lineSeparator().toCharArray();
    
    public PDFOrdinativo buildOrdinativo(String fileName) throws Exception {
        return buildOrdinativo(Files.readAllBytes(new File(fileName).toPath()));
    }
          
    public PDFOrdinativo buildOrdinativo(byte[] file) throws Exception {
        String text;
        int pagine;
        
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            text = pdfStripper.getText(document);
            pagine = document.getNumberOfPages();
        }
        
        if(text==null || !text.contains("GEOCOS - Ordinativi di contabilità speciale")) {
            return null;
        }
        
        PDFOrdinativo o = new PDFOrdinativo();
        o.setPagine(pagine);
        fillBean(text, TOKENS, o);
         
        // Processa estinzione
        Estinzione est = getModalitaEstizione(o.getModalitaEstinzione()); 
        if(est==null) throw new ImportException("Modalità estinzione non riconosciuta ["+o.getModalitaEstinzione()+"].");
        Token[] t = est.getTokens();
        if(t==null) throw new ImportException("Non sono stati configurati i token per la modalità estinzione ["+o.getModalitaEstinzione()+"].");
        fillBean(text, t, est);
        
        o.setEstinzione(est);
        return o;
    }
    
    public QuietanzaGeocos buildQuietanza(String fileName) throws Exception {
        return buildQuietanza(Files.readAllBytes(new File(fileName).toPath()));
    }
    
    public QuietanzaGeocos buildQuietanza(byte[] file) throws Exception {
        String text;
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            text = pdfStripper.getText(document);
        }
        catch(IOException ioe) {
            logger.error("Il file PDF non può essere elaborato a casua di un errore I/O inatteso.", ioe);
            return null;
        }
        
        if(text==null || !(text.startsWith("GEOCOS") && text.contains("Dettaglio Quietanza"))) return null;
        
        return fillBean(text, QTOKENS, QuietanzaGeocos.class);
    }
    
    @SuppressWarnings("empty-statement")
    public String getData(String data, String field, int maxLength) {
        int idx = data.indexOf(field);
        if(idx==-1) return null;
        idx+=field.length();
        String out = "";
        while(data.charAt(idx++)!=':');       
        if(maxLength!=-1) maxLength+=idx; else maxLength+=data.length()-idx;
        while(!isLineSep(data.charAt(idx)) && idx<maxLength) {
            out+=data.charAt(idx++);            
        }
        return out.trim();
    }
    
    public boolean isLineSep(char c) {
        for(char cx : LINE_SEP) {
            if(cx==c) return true;
        }
        return false;
    }
    
    @SuppressWarnings("empty-statement")
    public String getData(String data, String field, String next, int maxLength) {
        int idx = data.indexOf(field);
        int lastIdx = data.indexOf(next);
        if(idx==-1 || lastIdx<=idx) return null;
        idx+=field.length();
        String out = "";
        while(data.charAt(idx++)!=':');  
        if(maxLength!=-1 && (lastIdx-idx)>maxLength) lastIdx=idx+maxLength;
        while(idx<lastIdx) {
            char c = data.charAt(idx++);
            out+=c!='\r' && c!='\n' ? c : "";
        }
        return out.trim();
    }
    
    private void fillBean(String text, Token[] tokens, Object bean) throws IllegalAccessException {
        BeanFiller bf = new BeanFiller(bean);
                
        for(int i=0;i<tokens.length;i++) {            
            String tToLow = text.toLowerCase();
            int posStart = tToLow.indexOf(tokens[i].getToken().toLowerCase());
            // trovato?
            if(posStart>=0) {                
                int posEnd = (i<(tokens.length-1)) ? tToLow.indexOf(tokens[i+1].getToken().toLowerCase(), tokens[i].getToken().length()+1) : -1;                

                String value;
                // prima dell'ultimo token?
                if(posEnd>=0) {
                    value = text.substring(posStart+tokens[i].getLength(), posEnd).trim();
                    if(tokens[i].hasSplitChar()) {
                        String[] tokParts = tokens[i].getToken().split(tokens[i].getSplitChar());
                        String[] tokValues = value.split(tokens[i].getSplitChar());

                        for(int k=0;k<tokParts.length;k++) {
                            if(k<tokValues.length) bf.setProperty(Token.sanitize(tokParts[k].trim()), tokValues[k].trim());                                 
                        }
                    }
                    else {
                        bf.setProperty(tokens[i].getSanitizeed(), value);
                    }   
                    
                    posStart = posEnd-1;
                }
                else { // ultimo token
                    value = "";
                    for(int j=posStart+tokens[i].getLength();j<text.length();j++) {
                        if(text.charAt(j)==' ') break;
                        value += text.charAt(j);
                    }                        
                    bf.setProperty(tokens[i].getSanitizeed(), value);
                } 
                
                text = text.substring(posStart+1);
            }
        }
    }
    
    public <T> T fillBean(String data, QuietanzaToken[] tokens, Class<T> expectedType) throws IllegalAccessException, InstantiationException {
        T bean = expectedType.newInstance();
        BeanFiller bf = new BeanFiller(bean);

        for(QuietanzaToken t : tokens) {                
            String v = t.getNextToken()==null ? getData(data, t.getToken(), t.getTokenLength()) : getData(data, t.getToken(), t.getNextToken(), t.getTokenLength());

            bf.setProperty(sanitizeToken(t.getToken()), v);
        }

        return bean;        
    }
    
    public Estinzione getModalitaEstizione(String modalita) throws Exception {
        Reflections reflections = new Reflections("it.usr.pdfextract.model");
        Set<Class<?>> annotated = reflections.get(Scanners.SubTypes.of(Scanners.TypesAnnotated.with(ModalitaEstinzione.class)).asClass());
        for(Class c : annotated) {
            Annotation ann = c.getAnnotation(ModalitaEstinzione.class);
            Method m = ann.annotationType().getDeclaredMethod("value");
            String[] vList = (String[])m.invoke(ann, (Object[])null);
            for(String v : vList) {
                if(v.equalsIgnoreCase(modalita)) return (Estinzione)c.newInstance();
            }
        }
        
        return null;
    }
    
    public String sanitizeToken(String token) {
        return (token!=null) ? token.replace(" ", "").trim() : null;
    }       
}
