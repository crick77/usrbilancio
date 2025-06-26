/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package it.usr.web.usrbilancio.service;

/**
 *
 * @author riccardo.iovenitti
 */
public class IntegrityException extends RuntimeException {

    /**
     * Creates a new instance of <code>IntegrityException</code> without detail
     * message.
     */
    public IntegrityException() {
    }

    /**
     * Constructs an instance of <code>IntegrityException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public IntegrityException(String msg) {
        super(msg);
    }
}
