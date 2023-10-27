package model;

import grupo5.reto1.exceptions.ServerErrorException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Stack;

/**
 * Esta clase crea, cierra y gestiona las conexiones con la base de datos.
 *
 * @author Jason.
 */
public class Pool {

    private static Stack<Connection> connections = null;

    /**
     * Es el constructor del pool
     */
    public Pool() {
        connections = new Stack();
    }

    /**
     * Si no existe ninguna conexion libre en el stack crea una nueva y la
     * devuelve al usuario, si no coje la ultima del esta y la devuelve.
     *
     * @return una conexion a la base de datos.
     */
    public synchronized Connection getConnection() throws ServerErrorException {
        Connection con = null;

        try {

            if (connections.isEmpty()) {

                String URL = ResourceBundle.getBundle("files.config").getString("url");
                String USER = ResourceBundle.getBundle("files.config").getString("user");
                String PASSWORD = ResourceBundle.getBundle("files.config").getString("password");

                con = DriverManager.getConnection(URL, USER, PASSWORD);

            } else {
                con = connections.pop();
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
        connections.push(con);
    }

    /**
     * Cuando el servidor se cierre se cerraran todas las conexiones que se han
     * creado.
     */
    public synchronized void closeServer() throws ServerErrorException {
        try {

            for (int i = 0; i < connections.size(); i++) {
                Connection con = connections.pop();
                con.close();
            }

        } catch (SQLException e) {
            throw new ServerErrorException("Error a la hora de cerrar las conexiones");
        }
    }

}
