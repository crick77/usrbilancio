/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

import it.usr.web.controller.AbstractAuthController;
import it.usr.web.usrbilancio.domain.tables.records.UtenteRecord;
import it.usr.web.usrbilancio.service.UtenteService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@RequestScoped
public class AuthController extends AbstractAuthController {
    @Inject
    UtenteService us;

    @Override
    protected Object getUser(String username) {
        UtenteRecord u = us.getUtente(username);
        return (u!=null && u.getAbilitato()==1) ?  u : null;
    }      
}
