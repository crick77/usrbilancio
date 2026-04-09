/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.domain;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author riccardo.iovenitti
 */
@SessionScoped
@Named
public class ActiveUser implements Serializable {
    private AppUser currentUser;
    private final Map<String, Object> attributes = new HashMap<>();

    public AppUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(AppUser currentUser) {
        this.currentUser = currentUser;
    }            

    public Map<String, Object> getAttributes() {
        return attributes;
    }        
}
