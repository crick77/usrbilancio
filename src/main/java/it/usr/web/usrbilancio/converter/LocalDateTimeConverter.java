/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import java.time.LocalDate; 
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author riccardo.iovenitti
 */
@FacesConverter("localDateTimeConverter")
public class LocalDateTimeConverter implements Converter<LocalDateTime> {
    public final static String DATE_FORMAT = "dd-MM-yyyy";

    @Override
    public LocalDateTime getAsObject(FacesContext context, UIComponent component, String value) {
        if(value==null) return null;
        
        LocalDate ld = LocalDate.parse(value, DateTimeFormatter.ofPattern(DATE_FORMAT));
        return ld.atTime(0, 0);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, LocalDateTime value) {
        return (value!=null) ? value.format(DateTimeFormatter.ofPattern(DATE_FORMAT)) : null;
    }    
}
