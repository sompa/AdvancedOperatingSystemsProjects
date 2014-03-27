/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aos.pkgfinal;

/**
 *
 * @author Sompa
 */
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
            this.id = (clientID == null) ? (Integer) in.readObject() : clientID;
            if (selfID != null) {
                sendClientID(selfID);
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Message readMsg() {
        Message data = null;
        try {
            data = (Message) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
           // System.out.println("translations are so sucky");
//            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }

    public boolean writeMsg(Message data) {
        try {
            out.writeObject(data);
            out.flush();
        } catch (IOException ex) {
            System.out.println("he must have worked with you");
            //Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    private void sendClientID(Integer id) {
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
            //Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        process();
    }

    public boolean process() {
        Message msg = null;
        do {
            try {
                msg = (Message) in.readObject();
            } catch(NullPointerException ex) {
               // System.out.println("if i can learn a little bit of that then i can go there and i can be like i know shit already");
                return false;
            } catch (IOException ex) {
                System.out.println("io msg failed: " + this.id);
                //Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            } catch (ClassNotFoundException ex) {
                System.out.println("class msg failed: " + this.id);
                //Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while (handler.processMessage(msg, this));
        close();
        return true;
    }
}
