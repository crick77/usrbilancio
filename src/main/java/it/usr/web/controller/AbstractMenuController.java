/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.usr.web.controller;

import it.usr.web.controller.BaseController;
import java.util.List;

/**
 *
 * @author riccardo.iovenitti
 */
public abstract class AbstractMenuController extends BaseController {
    public void error() {
        throw new NumberFormatException("This is an error!");
    }

    public abstract List<MenuItem> getMenuItems();
}
