
package model;

import exceptions.CredentialErrorException;
import exceptions.InsertErrorException;
import exceptions.SelectErrorException;
import exceptions.ServerErrorException;
import exceptions.UserAlreadyExistsException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase es la implementación de la interfaz de lógica de negocio.
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

    private final String LOGIN_RES_USERS = "SELECT partner_id FROM res_users WHERE login = ? AND password = ?";
    private final String LOGIN_RES_PARTNER = "SELECT name, street, phone, zip FROM res_partner WHERE id = ?";

    
    /**
     * Este metodo coge una conexión del pool.
     * @throws ServerErrorException excepción de error de servidor.
     */
    public void openConnetion() throws ServerErrorException {
        this.pool = pool.getPool();

        conn = pool.getConnection();
    }

    /**
     * Este metodo devuelve la conexión al pool.
     * @throws ServerErrorException  excepción de error de servidor.
     */
    public void closeConnection() throws ServerErrorException {
        try {
            stmt.close();
            pool.closeServer();
        } catch (SQLException ex) {
            Logger.getLogger(DaoImplementation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    /**
     * Este metodo guarda los datos de registro de un usuario en la base de datos de odoo.
     * @param user un objeto usuario con los datos que queremos guardar.
     * @return user, devuelve el usuario
     * @throws UserAlreadyExistsException excepción de usuario existente.
     * @throws ServerErrorException excepción de error en el servidor.
     * @throws InsertErrorException  excepción de insertar datos en la base de datos de odoo.
     */
    @Override
    public User getExecuteSignUp(User user) throws UserAlreadyExistsException, ServerErrorException, InsertErrorException {
        this.openConnetion();

        ResultSet rs = null;

        try {
            if (comprobarUsuarioExistente(user.getEmail())) {
                throw new UserAlreadyExistsException(MessageType.USER_ALREADY_EXISTS_RESPONSE + "");
            }

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

                        if (idUser == null) {
                            throw new InsertErrorException("Ha ocurrido un error en la inserción, porque falta el ID usuario.");
                        }

                    }
                    stmt = conn.prepareStatement(INSERT_RES_GROUPS);

                    stmt.setString(1, idUser);

                    if (stmt.executeUpdate() == 1) {
                        String idPartner = null;

                        stmt = conn.prepareStatement(SELECT_MAX_USERS);
                        rs = stmt.executeQuery();

                        if (rs.next()) {
                            idPartner = rs.getString("id");
                            stmt = conn.prepareStatement(INSERT_RES_COMPANY);

                            stmt.setString(1, idPartner);

                            stmt.executeUpdate();
                        } else {
                            throw new InsertErrorException("Ha ocurrido un error en la inserción, porque falta el ID partner.");
                        }

                    }
                }
            }
        } catch (SQLException ex) {
            throw new ServerErrorException("Ha ocurrido un problema en el servidor");
        }
        this.closeConnection();
        return user;
    }

    /**
     * Este método busca el usuario en la base de datos, mediante el email y la contraseña. 
     * Si coincide, devuelve todos los datos del usuario.
     * @param user un objeto usuario
     * @return user , devuelve los datos del usuario.
     * @throws ServerErrorException excepción de error del servidor. 
     * @throws CredentialErrorException excepción de credenciales incorrectas.
     */
    
    @Override
    public User getExecuteSignIn(User user) throws ServerErrorException, CredentialErrorException {
        User u = null;
        this.openConnetion();

        try {
            stmt = conn.prepareStatement(LOGIN_RES_USERS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswd());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String partner_id = rs.getString("partner_id");

                stmt = conn.prepareStatement(LOGIN_RES_PARTNER);
                stmt.setString(1, partner_id);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    u = new User();
                    u.setName(rs.getString("name"));
                    u.setAddres(rs.getString("street"));
                    u.setPhone(rs.getInt("phone"));
                    u.setZip(rs.getInt("zip"));
                }
            }

        } catch (SQLException ex) {
            throw new CredentialErrorException("Ha ocurrido un error al iniciar sesion");
        }

        this.closeConnection();
        return u;

    }

    
    /**
     * Este método comprueba que si el usuario es existente.
     * @param email email para comprobarlo si existe en la base de datos.
     * @return existe, saber si el usuario existe o no.
     * @throws ServerErrorException excepción de error de servidor
     * @throws UserAlreadyExistsException excepcion de usuario existente.
     */
    private boolean comprobarUsuarioExistente(String email) throws ServerErrorException, UserAlreadyExistsException {
        this.openConnetion();

        boolean existe = false;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(USUARIO_EXISTE);

            stmt.setString(1, email);
            rs = stmt.executeQuery();

            if (rs.next()) {
                existe = true;
            }

        } catch (SQLException ex) {
            throw new UserAlreadyExistsException("Ese usuario ya existe");
        }

        this.closeConnection();
        return existe;
    }

}
