/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author Ian.
 */
public class DaoFactory {

    public static Signable getDao() {
        return new DaoImplementation();
    }
}