/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lamportme;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sompa
 */
interface NodeHandler {
    public boolean processMessage(Message msg, Node node);
}
public class Node extends Thread {
    Socket socket;
    Integer id;
    ObjectInputStream in;
    ObjectOutputStream out;
    NodeHandler handler;
   // private boolean keepRunning;
    
    public Node(Socket socket, NodeHandler handler) {
        this(socket, handler, null, null);
    }
    
    public Node(Socket socket, NodeHandler handler, Integer clientID) {
        this(socket, handler, clientID, null);
    }

    public Node(Socket socket, NodeHandler handler, Integer clientID, Integer selfID) {
        try {
            this.socket = socket;
            this.handler = handler;
            this.out = new ObjectOutputStream(socket.getOutputStream());            
            this.in = new ObjectInputStream(socket.getInputStream());
            this.id = (clientID == null) ? (Integer)in.readObject() : clientID;
            if(selfID!=null)
                sendClientID(selfID);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public Message readMsg() {
        Message data = null;
        try {
            data = (Message)in.readObject();
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    
    public void writeMsg(Message data) {
        try {
            out.writeObject(data);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void sendClientID(Integer id) {
        try {
            out.writeObject(id);
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        Message msg = null;
        do {
            try {
                msg = (Message)in.readObject();
            } catch (IOException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while(handler.processMessage(msg, this));
        close();
    }

    
}
