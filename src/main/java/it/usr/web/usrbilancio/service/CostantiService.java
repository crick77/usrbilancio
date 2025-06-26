/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author riccardo.iovenitti
 */
@Named
@ApplicationScoped
public class CostantiService {
    private final Map<Integer, String> FLAGS = new HashMap<>();
    
    @PostConstruct
    public void init() {
        FLAGS.put(0, "-");
        FLAGS.put(1, "COMMISSARIO");
    }

    public Map<Integer, String> getFLAGS() {
        return FLAGS;
    }        
}
