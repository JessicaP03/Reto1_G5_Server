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
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ian.
 */
public class DaoImplementation implements Signable{
    
    private Connection conn = null;
    private PreparedStatement stmt;
    private static Pool pool;
    private static final Logger LOG = Logger.getLogger(DaoImplementation.class.getName());
    
    private final String INSERT_RES_USERS = "INSERT INTO res_users(company_id, partner_id, create_date, login, password, create_uid, write_uid, write_date, notification_type) VALUES ( ?, ?, ?, ?, ?, 2, 3, now(), 'email');";
    private final String INSERT_RES_PARTNER = "INSERT INTO res_partner(company_id, create_date, name, parent_id, commercial_partner_id, create_uid, write_uid, display_name, ref, vat, street, zip, phone, date, active) VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?)";
    private final String INSERT_RES_COMPANY = "INSERT INTO res_company_users_rel(cid, user_id) VALUES (?, ?)";
    private final String INSERT_RES_GROUPS = "INSERT INTO res_groups_users_rel(gid, uid) VALUES (?, ?)";
    //private final String k;

    
    
   public void openConexion()  {
      this.pool = pool.getPool();
      
  }

    @Override
    public User getExecuteSignUp(User user) throws UserAlreadyExistsException, UserNotFoundException, ServerErrorException {
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(INSERT_RES_PARTNER);
            stmt.setInt(1, user.getCompany());
            stmt.setDate(2, Date.valueOf(user.getCreateDate()));
            stmt.setString(3, user.getName());
            stmt.setString(8, user.getName());
            stmt.setString(11, user.getAddress());
            stmt.setInt(12, user.getZip());
            stmt.setInt(13, user.getZip());
            stmt.setInt(14, user.getPhone());
            stmt.setBoolean(15, user.getActivo());
        } catch (Exception e) {
        }
       conn = pool.getConnection();
        try {
            stmt = conn.prepareStatement(INSERT_RES_USERS);
            stmt.setInt(1, user.getCompany());
            stmt.setDate(3, Date.valueOf(user.getCreateDate()));
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getPasswd());
            stmt.setDate(8, Date.valueOf(user.getWriteDate()));

            stmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(DaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User getExecuteSignIn(User user) throws ServerErrorException, CredentialErrorException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
}
