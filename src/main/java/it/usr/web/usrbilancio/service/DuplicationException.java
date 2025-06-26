/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package it.usr.web.usrbilancio.service;

/**
 *
 * @author riccardo.iovenitti
 */
public class DuplicationException extends RuntimeException {

    /**
     * Creates a new instance of <code>DuplicationException</code> without
     * detail message.
     */
    public DuplicationException() {
    }

    /**
     * Constructs an instance of <code>DuplicationException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public DuplicationException(String msg) {
        super(msg);
    }
}
