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
 *
 * @author Ian.
 */
public class WorkingThread extends Thread {

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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {

        try {
            ois = new ObjectInputStream(socket.getInputStream());
            DaoFactory factoria = new DaoFactory();
            sign = factoria.getDao();

            message = (Message) ois.readObject();

            switch (message.getMessageType()) {
                case SIGNIN_REQUEST:
                    user = sign.getExecuteSignIn(message.getUser());
                    message.setUser(user);
                    message.setMessageType(MessageType.OK_RESPONSE);
                    break;

                case SIGNUP_REQUEST:
                    sign.getExecuteSignUp(message.getUser());
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
            message.setMessageType(MessageType.ERROR_RESPONSE);
        } finally {
            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(message);
                Server.desconectarCliente(this);
                ois.close();
                oos.close();
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(WorkingThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

//    private Socket socketCliente;
//    private Message respuesta;
//
//    public WorkingThread(Socket socketCliente, Message respuesta) {
//        this.socketCliente = socketCliente;
//        this.respuesta = respuesta;
//    }
//
//
//
//    @Override
//    public void run() {
//        ObjectOutputStream oos;
//
//        try {
//            oos = new ObjectOutputStream(socketCliente.getOutputStream());
//            oos.writeObject(respuesta);
//        } catch (IOException ex) {
//            Logger.getLogger(WorkingThread.class.getName()).log(Level.SEVERE, null,ex);
//        }finally{
//
//        }
//    }
}
