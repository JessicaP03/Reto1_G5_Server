package model;

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
    private Message respuesta;

    public WorkingThread(Socket socketCliente, Message respuesta) {
        this.socketCliente = socketCliente;
        this.respuesta = respuesta;
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
