/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.converter;

import it.usr.web.usrbilancio.domain.tables.records.AnagraficaRecord;
import it.usr.web.usrbilancio.service.AnagraficaService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author riccardo.iovenitti
 */
@FacesConverter("anagraficaConverter")
public class AnagraficaConverter implements Converter<AnagraficaRecord> {
    @Inject
    AnagraficaService as;

    @Override
    public AnagraficaRecord getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            return (value==null) ? null : as.getAnagraficaById(Integer.parseInt(value));
        }
        catch(NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, AnagraficaRecord value) {
        return (value==null) ? null : String.valueOf(value.getId());
    }      
}
