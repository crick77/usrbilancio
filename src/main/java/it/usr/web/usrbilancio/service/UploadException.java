/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package it.usr.web.usrbilancio.service;

/**
 *
 * @author riccardo.iovenitti
 */
public class UploadException extends RuntimeException {

    /**
     * Creates a new instance of <code>UploadException</code> without detail
     * message.
     */
    public UploadException() {
    }

    /**
     * Constructs an instance of <code>UploadException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public UploadException(String msg) {
        super(msg);
    }
}
