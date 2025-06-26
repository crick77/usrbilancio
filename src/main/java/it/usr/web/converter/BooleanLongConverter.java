/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 *
 * @author riccardo.iovenitti
 */
@FacesConverter("booleanLongConverter")
public class BooleanLongConverter implements Converter<Long> {
    @Override
    public Long getAsObject(FacesContext fc, UIComponent uic, String string) {
        boolean b = Boolean.parseBoolean(string);
        return (b ? 1L : 0L);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Long t) {
        return t==0 ? "false" : "true";
    }    
}
