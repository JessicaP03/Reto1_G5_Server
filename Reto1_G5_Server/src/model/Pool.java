package model;

import exceptions.ServerErrorException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase crea, cierra y gestiona las conexiones con la base de datos.
 *
 * @author Jason.
 */
public class Pool {

    final private Logger LOGGER = Logger.getLogger(WorkingThread.class.getName());

    private static Stack<Connection> connections = null;

    /**
     * Es el constructor del pool
     */
    public Pool() {
        connections = new Stack();
    }

    /**
     * Si no existe ninguna conexión libre en el stack, crea una nueva y la
     * devuelve al usuario, si no, coje la ultima del esta y la devuelve.
     *
     * @return una conexion a la base de datos.
     * @throws exceptions.ServerErrorException controla un error a la hora de
     * conectarse o desconectarse con el servidor.
     */
    public synchronized Connection getConnection() throws ServerErrorException {
        Connection con = null;

        try {

            if (connections.isEmpty()) {
                String URL = ResourceBundle.getBundle("files.config").getString("url");
                String USER = ResourceBundle.getBundle("files.config").getString("user");
                String PASSWORD = ResourceBundle.getBundle("files.config").getString("password");

                con = DriverManager.getConnection(URL, USER, PASSWORD);

                LOGGER.log(Level.INFO, "El pool ha creado una conexion con los siguientes parametros\nURL: {0}\nUSER: {1}\nPASSWORD: {2}", new Object[]{URL, USER, PASSWORD});
            } else {
                con = connections.pop();
                LOGGER.log(Level.INFO, "El pool ha devuelto la siguiente conexion: " + con.toString());
            }

        } catch (Exception e) {
            throw new ServerErrorException("Error a la hora de crear una conexion");
        }

        return con;
    }

    /**
     * *
     * Cuando un usuario haya terminado, devuelve la conexion y la guardamos en
     * el stack.
     *
     * @param con la conexion que se va a devolver.
     */
    public void freeConnection(Connection con) {
        LOGGER.info("Se ha devuelto la conexion: " + con.toString() + " al pool");
        connections.push(con);
    }

    /**
     * Cuando el servidor se cierre, se cerraran todas las conexiones que se han
     * creado.
     *
     * @throws exceptions.ServerErrorException controla un error a la hora de
     * contectarse o desconectarse con el servidor.
     */
    public synchronized void closeServer() throws ServerErrorException {
        try {
            for (int i = 0; i < connections.size(); i++) {
                Connection con = connections.pop();
                con.close();
            }
            LOGGER.info("Se ha cerrado el servidor y se han limpiado todas las conexiones del pool");
        } catch (SQLException e) {
            throw new ServerErrorException("Error a la hora de cerrar las conexiones");
        }
    }

}
