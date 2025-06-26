/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author riccardo.iovenitti
 */
public class AppUser implements Serializable {
    private String username;    
    private Object attributes;

    public AppUser() {        
    }

    public AppUser(String username, Object attributes) {
        this.username = username;
        this.attributes = attributes;
    }
    
    
    public String getUsername() {
        return username;
    }
    
    public Object getAttributes() {
        return attributes;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.username);
        hash = 29 * hash + Objects.hashCode(this.attributes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AppUser other = (AppUser) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        return Objects.equals(this.attributes, other.attributes);
    }           
}
