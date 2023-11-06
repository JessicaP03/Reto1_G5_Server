package model;

import exceptions.CredentialErrorException;
import exceptions.InsertErrorException;
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
 *
 * @author Ian.
 */
public class DaoImplementation implements Signable {

    private Connection conn = null;
    private PreparedStatement stmt;
    private static Pool pool;
    private static final Logger LOGGER = Logger.getLogger(DaoImplementation.class.getName());

    /**
     * Sentencias SQL para la base de datos de odoo.
     */
    private final String INSERT_RES_USERS = "INSERT INTO res_users(company_id, partner_id, create_date, login, password, create_uid, write_uid, write_date, notification_type) VALUES ( ?, ?, ?, ?, ?, 2, 2, ?, 'email');";
    private final String INSERT_RES_PARTNER = "INSERT INTO res_partner(id,company_id, create_date, name, commercial_partner_id, street, zip, phone, date, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, now(), ?)";
    private final String INSERT_RES_COMPANY = "INSERT INTO res_company_users_rel(cid, user_id) VALUES (1, ?)";
    private final String INSERT_RES_GROUPS = "INSERT INTO res_groups_users_rel(gid, uid) VALUES (16, ?), (26, ?), (28,?), (31,?)";
    private final String SELECT_MAX_USERS = "SELECT max(id) as id from res_users";
    //private final String SELECT_IDPARTNER = "SELECT id from res_partner";
    private final String SELECT_MAX_PARTNER = "SELECT max(id) as id from res_partner";
    private final String USUARIO_EXISTE = "SELECT login from res_users where login =?";

    private final String LOGIN_RES_USERS = "SELECT partner_id FROM res_users WHERE login = ? AND password = ?";
    private final String LOGIN_RES_PARTNER = "SELECT name, street, phone, zip FROM res_partner WHERE id = ?";

    /**
     * Este metodo coge una conexión del pool.
     *
     * @throws ServerErrorException excepción de error de servidor.
     */
    public void openConnetion() throws ServerErrorException {
        try {
            pool = Pool.getPool();
            conn = pool.getConnection();

        } catch (ServerErrorException ex) {
            throw new ServerErrorException("Ha ocurrido un error a la hora de abrir la conexcion");
        }
    }

    /**
     * Este metodo devuelve la conexión al pool.
     *
     * @throws ServerErrorException excepción de error de servidor.
     */
    public void closeConnection() throws ServerErrorException {
        try {
            stmt.close();
            pool.closeServer();
        } catch (SQLException ex) {
            throw new ServerErrorException("Ha ocurrido un error a la hora de cerrar la conexcion");
        }
    }

    /**
     * Este metodo guarda los datos de registro de un usuario en la base de
     * datos de odoo.
     *
     * @param user un objeto usuario con los datos que queremos guardar.
     * @return user, devuelve el usuario
     * @throws UserAlreadyExistsException excepción de usuario existente.
     * @throws ServerErrorException excepción de error en el servidor.
     * @throws InsertErrorException excepción de insertar datos en la base de
     * datos de odoo.
     */
    @Override
    public User getExecuteSignUp(User user) throws UserAlreadyExistsException, ServerErrorException, InsertErrorException {
        LOGGER.info("Entro en el DAOImplementacion SIGN UP");

        this.openConnetion();

        int partnerId = 0;
        ResultSet rs = null;

        try {
            if (comprobarUsuarioExistente(user.getEmail())) {
                throw new UserAlreadyExistsException(MessageType.USER_ALREADY_EXISTS_RESPONSE + "");
            }

            stmt = conn.prepareStatement(SELECT_MAX_PARTNER);

            rs = stmt.executeQuery();

            if (rs.next()) {
                partnerId = rs.getInt("id");

                if (partnerId == 0) {
                    throw new InsertErrorException("Ha ocurrido un error en la inserción, porque falta el ID usuario.");
                }

            }

            stmt = conn.prepareStatement(INSERT_RES_PARTNER);
            stmt.setInt(1, partnerId + 1);
            stmt.setInt(2, user.getCompany());
            stmt.setDate(3, Date.valueOf(user.getCreateDate()));
            stmt.setString(4, user.getName());
            stmt.setInt(5, partnerId + 1);
            stmt.setString(6, user.getAddress());
            stmt.setInt(7, user.getZip());
            stmt.setInt(8, user.getPhone());
            stmt.setBoolean(9, user.getActivo());

            if (stmt.executeUpdate() == 1) {

                stmt = conn.prepareStatement(INSERT_RES_USERS);
                stmt.setInt(1, user.getCompany());
                stmt.setInt(2, partnerId + 1);
                stmt.setDate(3, Date.valueOf(user.getCreateDate()));
                stmt.setString(4, user.getEmail());
                stmt.setString(5, user.getPasswd());
                stmt.setDate(6, Date.valueOf(user.getWriteDate()));

                if (stmt.executeUpdate() == 1) {
                    int idUser = 0;

                    rs = stmt.executeQuery();

                    if (rs.next()) {
                        idUser = rs.getInt("id");

                        if (idUser == 0) {
                            throw new InsertErrorException("Ha ocurrido un error en la inserción, porque falta el ID usuario.");
                        }

                    }
                    stmt = conn.prepareStatement(INSERT_RES_GROUPS);

                    stmt.setInt(1, idUser);
                    stmt.setInt(2, idUser);
                    stmt.setInt(3, idUser);
                    stmt.setInt(4, idUser);

                    if (stmt.executeUpdate() == 1) {

                        stmt = conn.prepareStatement(INSERT_RES_COMPANY);
                        stmt.setInt(1, idUser);
                        stmt.executeUpdate();

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
     * Este método busca el usuario en la base de datos, mediante el email y la
     * contraseña. Si coincide, devuelve todos los datos del usuario.
     *
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
            LOGGER.info("Entra en el DAOImplementacion SIGN IN");

            stmt = conn.prepareStatement(LOGIN_RES_USERS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswd());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int partner_id = rs.getInt("partner_id");

                stmt = conn.prepareStatement(LOGIN_RES_PARTNER);
                stmt.setInt(1, partner_id);

                ResultSet rs2 = stmt.executeQuery();

                if (rs2.next()) {
                    u = new User();
                    u.setEmail(user.getEmail());
                    u.setPasswd(user.getPasswd());
                    u.setName(rs2.getString("name"));
                    u.setAddress(rs2.getString("street"));
                    u.setPhone(rs2.getInt("phone"));
                    u.setZip(rs2.getInt("zip"));
                }
            } else {
                throw new CredentialErrorException("El usuario y la contraseña no coinciden");
            }

        } catch (SQLException ex) {
            throw new CredentialErrorException("Ha ocurrido un error al iniciar sesion");
        }

        this.closeConnection();
        return u;

    }

    /**
     * Este método comprueba que si el usuario es existente.
     *
     * @param email email para comprobarlo si existe en la base de datos.
     * @return existe, saber si el usuario existe o no.
     * @throws ServerErrorException excepción de error de servidor
     * @throws UserAlreadyExistsException excepcion de usuario existente.
     */
    private boolean comprobarUsuarioExistente(String email) throws ServerErrorException, UserAlreadyExistsException {
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

        return existe;
    }

}
