/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import exceptions.CredentialErrorException;
import exceptions.ServerErrorException;
import exceptions.UserAlreadyExistsException;
import exceptions.UserNotFoundException;
import model.Signable;
import model.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 *
 * @author Ian.
 */
public class DaoImplementation implements Signable{
    
    private Connection conn = null;
    private PreparedStatement stmt;
    //private static Pool pool;
    private static final Logger LOG = Logger.getLogger(DaoImplementation.class.getName());
    
    private final String INSERT_RES_USERS = "INSERT INTO res_users(company_id, partner_id, create_date, login, password, create_uid, write_uid, write_date, notification_type) VALUES ( 1, ?, now(), ?, ?, 2, 3, now(), 'email');";
    private final String INSERT_RES_PARTNER = "INSERT INTO res_partner(company_id, create_date, name, parent_id, commercial_partner_id, create_uid, write_uid, display_name, ref, vat, street, zip, phone, date, active) VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final String INSERT_RES_COMPANY = "INSERT INTO res_company_users_rel(cid, user_id) VALUES (?, ?)";
    private final String INSERT_RES_GROUPS = "INSERT INTO res_groups_users_rel(gid, uid) VALUES (?, ?)";
    //private final String k;

    
    
//    public void openConexion()  {
//       this.pool = pool.getPool();
//      
//    }

    @Override
    public User getExecuteSignUp(User user) throws UserAlreadyExistsException, UserNotFoundException, ServerErrorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User getExecuteSignIn(User user) throws ServerErrorException, CredentialErrorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
}
