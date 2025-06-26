/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package it.usr.web.usrbilancio.service;

/**
 *
 * @author riccardo.iovenitti
 */
public class StaleRecordException extends RuntimeException {

    /**
     * Creates a new instance of <code>StaleRecordException</code> without
     * detail message.
     */
    public StaleRecordException() {
    }

    /**
     * Constructs an instance of <code>StaleRecordException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public StaleRecordException(String msg) {
        super(msg);
    }
}
