/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.converter;

import it.usr.web.usrbilancio.domain.tables.records.OrdinativoRecord;
import it.usr.web.usrbilancio.service.OrdinativoService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author riccardo.iovenitti
 */
@FacesConverter("ordinativoConverter")
public class OrdinativoConverter implements Converter<OrdinativoRecord> {
    @Inject
    OrdinativoService os;
    
    @Override
    public OrdinativoRecord getAsObject(FacesContext fc, UIComponent uic, String string) {
        try {
            return (string==null) ? null : os.getOrdinativoById(Integer.parseInt(string));
        }
        catch(NumberFormatException e) {
            return null;
        }            
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, OrdinativoRecord t) {
        return (t==null) ? null : String.valueOf(t.getId());
    }
    
}
