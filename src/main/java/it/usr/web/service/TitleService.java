/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.service;

import jakarta.enterprise.context.Dependent;

/**
 *
 * @author riccardo.iovenitti
 */
@Dependent
public class TitleService {
    public String getTitle() {
        return "USR GENERICO";
    }
}
