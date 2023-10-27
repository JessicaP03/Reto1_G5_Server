package model;

import grupo5.reto1.model.MessageType;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ian.
 */
public class WorkingThread extends Thread {
    
    private Socket socketCliente;
    MessageType respuesta;

    public WorkingThread(Socket socketCliente, MessageType respuesta) {
        this.socketCliente = socketCliente;
        this.respuesta = respuesta;
    }
    
    

    WorkingThread(Message message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        ObjectOutputStream oos;
        
        try {
            oos = new ObjectOutputStream(socketCliente.getOutputStream());
            oos.writeObject(respuesta);
        } catch (IOException ex) {
            Logger.getLogger(WorkingThread.class.getName()).log(Level.SEVERE, null,ex);
        }finally{
            
        }
    }
}
