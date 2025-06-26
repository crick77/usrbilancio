/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.controller;

import java.io.Serializable;

/**
 *
 * @author riccardo.iovenitti
 */
public class SubMenuItem implements Serializable {
    private final String menuItem;
    private final String page;

    public SubMenuItem(String menuItem, String page) {
        this.menuItem = menuItem;
        this.page = page;
    }

    public String getMenuItem() {
        return menuItem;
    }

    public String getPage() {
        return page;
    }            
}
