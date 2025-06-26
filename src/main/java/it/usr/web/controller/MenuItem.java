/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author riccardo.iovenitti
 */
public class MenuItem implements Serializable {    
    private final String menuName;
    private final String page;
    private final List<SubMenuItem> subMenu = new ArrayList<>();

    public MenuItem(String menuName) {
        this(menuName, null, null);
    }
    
    public MenuItem(String menuName, List<SubMenuItem> subMenu) {
        this(menuName, null, subMenu);
    }
    
    public MenuItem(String menuName, String page, List<SubMenuItem> subMenu) {
        this.menuName = menuName;
        this.page = page;
        if(subMenu!=null) this.subMenu.addAll(subMenu);
    }

    public String getPage() {
        return page;
    }

    public String getMenuName() {
        return menuName;
    }            

    public List<SubMenuItem> getSubMenu() {
        return subMenu;
    }        
}
