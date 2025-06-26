/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.usrbilancio.domain.tables.records.CodiceRecord;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ApplicationScoped
public class Formatter {       
    public static String formattaCodice(CodiceRecord c) {        
        StringJoiner sj = new StringJoiner(".");
        sj.add(c.getCodice());
        for(int i=1;i<=5;i++) {
            try {
                String fName = "getC"+((i<10) ? "0" : "")+i;
                Method m = c.getClass().getDeclaredMethod(fName);               
                Object _o = m.invoke(c);
                if(_o==null) break;
                sj.add(String.valueOf(_o));
            }
            catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                break;
            }
        }
        return sj.toString();
    }
    
    public String formattaCodiceRecord(CodiceRecord c) {
        return formattaCodice(c);
    }
    
    public String codiceComposto(CodiceRecord c) {
        return (c!=null) ? formattaCodice(c) : null;
    }
    
    public String formattaData(LocalDate d) {
        return (d!=null) ? d.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : null;
    }     
    
    public String formattaDecimale(BigDecimal d) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return (d!=null) ? df.format(d) : String.valueOf(d);
    }
}
