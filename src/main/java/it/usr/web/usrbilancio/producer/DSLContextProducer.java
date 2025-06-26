/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.usrbilancio.producer;

import jakarta.annotation.Resource;
import jakarta.enterprise.inject.Produces;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;

/**
 *
 * @author riccardo.iovenitti
 */
public class DSLContextProducer {
    @Resource(lookup = "jdbc/usrbilancio")
    DataSource ds;
    
    @Produces
    @DSLBilancio
    public DSLContext getContext() {
        return org.jooq.impl.DSL.using(ds, SQLDialect.MYSQL);
    }
}
