/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.pdfextract;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author riccardo.iovenitti
 */
public class BeanFiller {
    private final Object instance;
    private final Class clazz;
    private final Map<String, Field> _fields = new HashMap<>();
    private final DecimalFormat decimalFormat;
    private final SimpleDateFormat simpleDateFormat;
    
    public BeanFiller(Object _instance) {
        this.instance = _instance;
        this.clazz = _instance.getClass();
        
        Field[] fs = clazz.getDeclaredFields();
        for(Field f : fs) {
            _fields.put(f.getName().toLowerCase(), f);
        }
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        String pattern = "#,##0.0#";
        decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);
        
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    }
    
    public boolean setProperty(String propertyName, Object value) throws IllegalAccessException {
        Field f = _fields.get(propertyName.toLowerCase());
        if(f==null) {           
            return false;
        }
        
        f.setAccessible(true);
        Object v;
        String sV = String.valueOf(value);
        switch(f.getType().getName()) {
            case "java.lang.Integer": {
                v = (sV.length()>0) ? Integer.valueOf(sV) : null;
                break;
            }
            case "int": {
                v = Integer.valueOf(sV);
                break;
            }
            case "java.lang.Float": {
                v = (sV.length()>0) ? Float.valueOf(sV) : null;
                break;            
            }
            case "float": {
                v = Float.valueOf(sV);
                break;
            }
            case "java.lang.Double": {
                v = (sV.length()>0) ? Double.valueOf(sV) : null;
                break;
            }
            case "double": {
                v = Double.valueOf(sV);
                break;
            }
            case "java.math.BigDecimal": {                
                try {
                    v = decimalFormat.parse(sV);
                    break;
                } catch (ParseException ex) {
                    return false;
                }                                
            }
            case "java.util.Date": {
                try {
                    v = simpleDateFormat.parse(sV);
                    break;
                } catch (ParseException ex) {
                    return false;
                }
            }
            default: {
                v = value;
            }

        }
        f.set(instance, v);
        
        return true;
    }
}
