/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.math.BigDecimal;
import org.primefaces.model.SortMeta;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ApplicationScoped
public class Sorter {
    public int sortStringAsInt(Object obj1, Object obj2, SortMeta meta){
        String s1 = String.valueOf(obj1);
        String s2 = String.valueOf(obj2);
        
        try {            
            int id1 = Integer.parseInt(s1);
            int id2 = Integer.parseInt(s2);
            if(id1 < id2){
                return -1;
            }else if(id1 == id2){
                return 0;
            }else{
                return 1;
            }
        }
        catch(NumberFormatException nfe) {
            return compare(s1, s2);
        }
    }
    
    public int sortStringAsBigDecimal(Object obj1, Object obj2, SortMeta meta){
        String s1 = String.valueOf(obj1);
        String s2 = String.valueOf(obj2);
        
        try {            
            BigDecimal id1 = new BigDecimal(s1);
            BigDecimal id2 = new BigDecimal(s2);
            return id1.compareTo(id2);
        }
        catch(NumberFormatException nfe) {
            return compare(s1, s2);
        }
    }
    
    public int compare(String str1, String str2) {
        return (str1!=null) ? str1.compareToIgnoreCase(str2) : -1;
    }
}
