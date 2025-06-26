/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 *
 * @author riccardo.iovenitti
 */
@FacesConverter("booleanIntConverter")
public class BooleanIntConverter implements Converter<Integer> {    
    @Override
    public Integer getAsObject(FacesContext fc, UIComponent uic, String string) {
        try {
            return (string==null || "false".equalsIgnoreCase(string)) ? 0 : 1;
        }
        catch(NumberFormatException e) {
            return null;
        }            
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Integer t) {
        return (t==null || t==0) ? "false" : "true";
    } 
}
