/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lamportme;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sompa
 */
public class ServerInstance implements NodeHandler {

    NodeInfo myServerNodeInfo;
    List<NodeInfo> serverInfos;
    Map<Integer, Node> servers;
    String homeFolder;
    File[] files;

    private ServerInstance(String[] args) {
        homeFolder = args[0];
        serverInfos = NodeInfo.readNodeInfos(args[1]);
        myServerNodeInfo = serverInfos.get(Integer.parseInt(args[2]));
        Connector serverConnector = new Connector(serverInfos, myServerNodeInfo, this);
        servers = serverConnector.makeAllConnections();
        files = new File(homeFolder).listFiles();
    }

    void recievedEnquire(Message msg, Node node) {
        System.err.println("Enquire msg: " + msg.getClientID() + " " + node.id);
        Message reply = new Message(files);
        node.writeMsg(reply);
    }

    void recievedReWrite(Message msg) {
        System.err.println("ReWrite msg: " + msg.getClientID() + " ");
        PrintStream out = null;
        try {
            out = new PrintStream(new BufferedOutputStream(new FileOutputStream(files[msg.getFileID()], true)));
            out.println(msg.getClientID() + "," + msg.getTimeStamp());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerInstance.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    void recievedWrite(Message msg, Node node) {
        System.err.println("Write msg: " + msg.getClientID() + " " + node.id);
        sendReWriteMessageToAll(msg, servers.values());
        recievedReWrite(msg);
        node.writeMsg(new Message(Message.WRITE_REPLY, msg.getFileID(), myServerNodeInfo.id, msg.getTimeStamp()));
    }

    void recievedRead(Message msg, Node node) {
        System.err.println("Read msg: " + msg.getClientID() + " " + node.id);
        String replyMsg = readLastLine(files[msg.getFileID()]);
        node.writeMsg(new Message(replyMsg));
    }

    private void process() {
        startReadingServerMessages();
        startProcessingClientMessages();
    }

    private void sendReWriteMessageToAll(Message msg, Collection<Node> servers) {
        System.err.println("Sending rewrite to all: " + msg.getClientID());
        msg.setType(Message.REWRITE);
        for (Node server : servers) {
            server.writeMsg(msg);
        }
    }

    private void startProcessingClientMessages() {
        System.err.println("start processing client message: ");
        try {
            ServerSocket serverSocket = new ServerSocket(myServerNodeInfo.port);
            while (true) {
                new Node(serverSocket.accept(), this).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startReadingServerMessages() {
        System.err.println("start reading processing server message: ");
        for (Node node : servers.values()) {
            node.start();
        }
    }

    private String readLastLine(File fileName) {
        System.err.println("filename: " + fileName.getName());
        BufferedReader br = null;
        String lastLine = null, tmp;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            while ((tmp = br.readLine()) != null) {
                lastLine = tmp;
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerInstance.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerInstance.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return lastLine;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("USAGE ServerInstance HomeFolder ServerFile ServerID");
            return;
        }
        new ServerInstance(args).process();
    }

    @Override
    synchronized public boolean processMessage(Message msg, Node node) {
        switch (msg.getType()) {
            case Message.ENQUIRE:
                recievedEnquire(msg, node);
                return false;
            case Message.WRITE:
                recievedWrite(msg, node);
                return false;
            case Message.REWRITE:
                recievedReWrite(msg);
                return true;
            case Message.READ:
                recievedRead(msg, node);
                return false;
        }
        return false;
    }
}
