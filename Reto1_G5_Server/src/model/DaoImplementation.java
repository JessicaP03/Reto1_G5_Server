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
public class DaoImplementation implements Signable {

    private Connection conn = null;
    private PreparedStatement stmt;
    private static Pool pool;
    private static final Logger LOG = Logger.getLogger(DaoImplementation.class.getName());

    private final String INSERT_RES_USERS = "INSERT INTO res_users(company_id, partner_id, create_date, login, password, create_uid, write_uid, write_date, notification_type) VALUES ( ?, ?, ?, ?, ?, 2, 3, now(), 'email');";
    private final String INSERT_RES_PARTNER = "INSERT INTO res_partner(company_id, create_date, name, parent_id, commercial_partner_id, street, zip, phone, date, active) VALUES (1, ?, ?, ?, ?, ?, ?, ?, now(), ?)";
    private final String INSERT_RES_COMPANY = "INSERT INTO res_company_users_rel(cid, user_id) VALUES (1, ?)";
    private final String INSERT_RES_GROUPS = "INSERT INTO res_groups_users_rel(gid, uid) VALUES (16, ?), (26, ?), (28,?), (31,?)";
    private final String SELECT_MAX_USERS = "SELECT max(id) as id from res_users";
    private final String SELECT_MAX_PARTNER = "SELECT max(id) as id from res_partner";
    private final String USUARIO_EXISTE = "SELECT login from res_users where login =?";

    public void openConexion() {
        this.pool = pool.getPool();

    }

    @Override
    public User getExecuteSignUp(User user) throws UserAlreadyExistsException, ServerErrorException {
        ResultSet rs = null;
        
        conn = pool.getConnection();
        if(comprobarUsuarioExistente(user.getEmail())){
            throw new UserAlreadyExistsException(USUARIO_EXISTE);
        }else{

        try {
            stmt = conn.prepareStatement(INSERT_RES_PARTNER);
            stmt.setInt(1, user.getCompany());
            stmt.setDate(2, Date.valueOf(user.getCreateDate()));
            stmt.setString(3, user.getName());
            stmt.setString(4, user.getName());
            stmt.setString(5, user.getAddress());
            stmt.setInt(6, user.getZip());
            stmt.setInt(7, user.getPhone());
            stmt.setBoolean(8, user.getActivo());

            if (stmt.executeUpdate() == 1) {

                stmt = conn.prepareStatement(INSERT_RES_USERS);
                stmt.setInt(1, user.getCompany());
                stmt.setDate(3, Date.valueOf(user.getCreateDate()));
                stmt.setString(4, user.getEmail());
                stmt.setString(5, user.getPasswd());
                stmt.setDate(6, Date.valueOf(user.getWriteDate()));

                if (stmt.executeUpdate() == 1) {
                     String idUser = null;
                    
                    stmt = conn.prepareStatement(SELECT_MAX_PARTNER);
                    rs = stmt.executeQuery();
                    if (rs.next()) {
                        idUser = rs.getString("id");
                    }else if(idUser == null){
                        throw new SQLException("Ha ocurrido un error en la inserción");
                    }
                    stmt = conn.prepareStatement(INSERT_RES_GROUPS);

                    stmt.setString(1, idUser);

                    
                         
                    if (stmt.executeUpdate() == 1) {
                        String idPartner = null;
                       
                        stmt = conn.prepareStatement(SELECT_MAX_USERS);
                        rs = stmt.executeQuery();
                        if (rs.next()) {
                        idPartner = rs.getString("id");
                    }else if(idPartner == null){
                        throw new SQLException("Ha ocurrido un error en la inserción");
                    }
                    stmt = conn.prepareStatement(INSERT_RES_COMPANY);

                    stmt.setString(1, idPartner);

                    stmt.executeUpdate();

                }
                }

            }
            
        } catch (SQLException ex) {
                Logger.getLogger(DaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
                throw new ServerErrorException("Ha ocurrido un problema en el servidor");
        } finally{
              try {
                pool.closeServer();
                if(stmt !=null){
                    try {
                        stmt.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(DaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (ServerErrorException ex) {
                Logger.getLogger(DaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return user;
            
        }
        
    }

    @Override
    public User getExecuteSignIn(User user) throws ServerErrorException, CredentialErrorException {
        
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean comprobarUsuarioExistente(String email) {
        boolean existe = false;
        ResultSet rs = null;
        try {
            conn = pool.getConnection();
            
            stmt = conn.prepareStatement(USUARIO_EXISTE);
            
            stmt.setString(1, email);
            rs = stmt.executeQuery();
            if(rs.next()){
                existe = true;
            }
            
        } catch (ServerErrorException ex) {
            Logger.getLogger(DaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
            
        } catch (SQLException ex) {
            Logger.getLogger(DaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                pool.closeServer();
                if(stmt !=null){
                    try {
                        stmt.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(DaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (ServerErrorException ex) {
                Logger.getLogger(DaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
        return existe;
    }

}
