package model;

import exceptions.CredentialErrorException;
import exceptions.InsertErrorException;
import exceptions.ServerErrorException;
import exceptions.UserAlreadyExistsException;
import exceptions.UserNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esta clase es un hilo, que permite a varios usuario a la vez, acceder a la
 * base de datos de odoo.
 *
 * @author Ian.
 */
public class WorkingThread extends Thread {

    final private Logger LOGGER = Logger.getLogger(WorkingThread.class.getName());

    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;
    private Socket socket = null;
    private Message message;
    private Signable sign;
    private User user = null;

    public WorkingThread() {

    }

    public WorkingThread(Socket socket) {
        this.socket = socket;
    }

    WorkingThread(Message message) {
        this.message = message;
    }

    /**
     * Este método se utiliza para ejecutar el hilo. En base a la petición que
     * se le haya hecho, inicia sesión o registra el usuario.
     */
    @Override
    public void run() {
        LOGGER.info("Se ha creado un hilo");
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            sign = DaoFactory.getDao();

            message = (Message) ois.readObject();

            switch (message.getMessageType()) {
                case SIGNIN_REQUEST:
                    LOGGER.info("Peticion del servidor: " + message.getMessageType() + " --> " + "SIGNIN_REQUEST");

                    user = sign.getExecuteSignIn(message.getUser());
                    message.setUser(user);
                    message.setMessageType(MessageType.OK_RESPONSE);
                    break;

                case SIGNUP_REQUEST:
                    LOGGER.info("Peticion del servidor: " + message.getMessageType() + " --> " + "SIGNUP_REQUEST");

                    user = sign.getExecuteSignUp(message.getUser());
                    message.setUser(user);
                    message.setMessageType(MessageType.OK_RESPONSE);
                    break;
            }

        } catch (IOException e) {
            message.setMessageType(MessageType.ERROR_RESPONSE);
        } catch (ClassNotFoundException ex) {
            message.setMessageType(MessageType.ERROR_RESPONSE);
        } catch (ServerErrorException ex) {
            message.setMessageType(MessageType.SERVER_ERROR);
        } catch (CredentialErrorException ex) {
            message.setMessageType(MessageType.CREDENTIAL_ERROR);
        } catch (UserAlreadyExistsException ex) {
            message.setMessageType(MessageType.USER_ALREADY_EXISTS_RESPONSE);
        } catch (UserNotFoundException ex) {
            message.setMessageType(MessageType.USER_NOT_FOUND_RESPONSE);
        } catch (InsertErrorException ex) {
            message.setMessageType(MessageType.USER_ALREADY_EXISTS_RESPONSE);
        } finally {
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(message);
                SocketServer.desconectarCliente(this);
                ois.close();
                oos.close();

                LOGGER.info("Se ha matado el hilo");
                socket.close();

            } catch (IOException ex) {
                Logger.getLogger(WorkingThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
