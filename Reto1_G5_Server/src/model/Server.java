package model;

import grupo5.reto1.model.Encapsulator;
import grupo5.reto1.model.Message;
import grupo5.reto1.model.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase crea el servidor donde los usuarios se podran conectar para hacer
 * las diferentes acciones.
 *
 * @author Jason.
 */
public class Server {

    final private Logger LOGGER = Logger.getLogger(Server.class.getName());

    final private int PORT = Integer.parseInt(ResourceBundle.getBundle("files.config").getString("port"));
    final private int MAX_USERS = Integer.parseInt(ResourceBundle.getBundle("files.config").getString("max_users"));
    private int num_users = 0;
    private boolean serverAbierto = true;

    /**
     * Abre el servidor, y cuando se conecte un usuario creamos un hilo para que
     * haga el trabajo.
     */
    public void openServer() {
        Socket client = null;
        ServerSocket server = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            LOGGER.info("El servidor se ha abierto");
            server = new ServerSocket(PORT);

            while (serverAbierto) {

                if (num_users < MAX_USERS) {

                    client = server.accept();
                    LOGGER.info("Un cliente se ha conectado.");
                    Message message = (Message) ois.readObject();
                    WorkingThread w1 = new WorkingThread(message);
                    w1.run();
                    conectarCliente();

                } else {
                    oos = new ObjectOutputStream(client.getOutputStream());
                    Encapsulator encapsulator = new Encapsulator();
                    encapsulator.setMessage(MessageType.MAX_USERS_CONECTED);
                    oos.writeObject(encapsulator);
                }
                desconectarCliente();
            }

        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sumamos uno a una variable que contiene la cantidad de usuarios que hay
     * conectados al mismo tiempo.
     */
    public synchronized void conectarCliente() {
        num_users++;
    }

    /**
     * Restamos uno a una variable que contiene la cantidad de usuarios que hay
     * conectados al mismo tiempo.
     */
    public synchronized void desconectarCliente() {
        num_users--;
    }

    /**
     * Crea e instancia la clase servidor y llama al metodo que lo abre
     *
     * @see Server.openServer()
     * @param args
     */
    public static void main(String[] args) {
        Server s1 = new Server();
        s1.openServer();
    }

}
