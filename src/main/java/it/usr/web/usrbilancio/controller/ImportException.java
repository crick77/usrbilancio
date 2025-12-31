/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package it.usr.web.usrbilancio.controller;

/**
 *
 * @author riccardo.iovenitti
 */
public class ImportException extends Exception {

    /**
     * Creates a new instance of <code>ImportException</code> without detail
     * message.
     */
    public ImportException() {
    }

    /**
     * Constructs an instance of <code>ImportException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public ImportException(String msg) {
        super(msg);
    }
}
