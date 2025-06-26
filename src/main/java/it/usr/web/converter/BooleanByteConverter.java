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
@FacesConverter("booleanByteConverter")
public class BooleanByteConverter implements Converter<Byte> {
    @Override
    public Byte getAsObject(FacesContext fc, UIComponent uic, String string) {
        boolean b = Boolean.parseBoolean(string);
        return (byte)(b ? 1 : 0);
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Byte t) {
        return t==0 ? "false" : "true";
    }    
}
