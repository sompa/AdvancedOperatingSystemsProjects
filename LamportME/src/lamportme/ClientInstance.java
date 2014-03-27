/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lamportme;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientInstance extends Thread implements NodeHandler {

    NodeInfo myNodeInfo;
    List<NodeInfo> clientInfos;
    List<NodeInfo> serverInfos;
    Map<Integer, Node> clients;
    String[] files;
    static int timestamp;
    int ackCounter;
    Map<Integer, PriorityBlockingQueue<Message>> fileAccessReq = new HashMap<Integer, PriorityBlockingQueue<Message>>();
    final private int GENERATE_REQUEST = -1;

    public ClientInstance(String[] args) {
        serverInfos = NodeInfo.readNodeInfos(args[0]);
        clientInfos = NodeInfo.readNodeInfos(args[1]);
        myNodeInfo = clientInfos.get(Integer.parseInt(args[2]));
        Connector clientConnector = new Connector(clientInfos, myNodeInfo, this);
        clients = clientConnector.makeAllConnections();        
    }

    synchronized public void process() {
        startReadingClientMessages();
        enquireServer();
        try {
            this.wait();
            Thread.sleep(5000);
//            while (true) {
            for(int i=0;i<100;i++) {
                processMessage(new Message(GENERATE_REQUEST, myNodeInfo.id, timestamp), null);
                this.wait();
                Thread.sleep(300);
            }
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    synchronized void recievedEnquireReply(Message msg) {
        this.files = msg.getFiles();
        System.out.println("files: " + this.files.length);
        for (int i = 0; i < files.length; i++) {
            System.out.println("file[" + i + "] = " + files[i]);
            fileAccessReq.put(i, new PriorityBlockingQueue<Message>());
        }
        this.notify();
    }

    void recievedRequest(Message msg, Node node) {
        fileAccessReq.get(msg.getFileID()).offer(msg);
        node.writeMsg(new Message(Message.ACKNOWLEDGEMENT, msg.getFileID(), myNodeInfo.id, updatedTimeStamp()));
    }

    void recievedRelease(Message msg) {
        Message m = fileAccessReq.get(msg.getFileID()).poll();
//        System.err.println("m f = " + m.getFileID() + " c = " + m.getClientID() + " ts = " + m.getTimeStamp());
        if ((fileAccessReq.get(msg.getFileID()).peek() != null) && ackCounter == clients.size() && fileAccessReq.get(msg.getFileID()).peek().getClientID() == myNodeInfo.id) {
            sendWriteRequest(msg.getFileID());
        }        
    }

    void recievedAcknowledgement(Message msg) {
        ackCounter++;
        if (ackCounter == clients.size() && fileAccessReq.get(msg.getFileID()).peek().getClientID() == myNodeInfo.id) {
            sendWriteRequest(msg.getFileID());
        }
    }

    void recievedReadReply(Message msg) {
        System.out.println("Line read from file is: " + msg.getMessage());
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("USAGE ClientInstance ServerFile ClientFile ClientID");
            return;
        }
        new ClientInstance(args).process();
    }

    synchronized void recievedWriteReply(Message msg) {
        System.out.println("Data has been written to file " + files[msg.getFileID()]);
        fileAccessReq.get(msg.getFileID()).poll();
        sendMessageToAll(new Message(Message.RELEASE, msg.getFileID(), myNodeInfo.id, updatedTimeStamp()));
        this.notify();
    }

    int updatedTimeStamp() {
        timestamp += myNodeInfo.id + 1;
        return timestamp;
    }

    private void startReadingClientMessages() {
        for (Node node : clients.values()) {
            node.start();
        }
    }

    void updatedTimeStamp(int timestamp) {
        this.timestamp = (this.timestamp > timestamp) ? this.timestamp : timestamp;
        updatedTimeStamp();
    }

    private void sendMessageToAll(Message msg) {
//        System.err.println("sent type = " + msg.getType() + " fid = " + msg.getFileID() + " id = " + msg.getClientID() + " ts = " + msg.getTimeStamp());
        for (Node node : clients.values()) {
            node.writeMsg(msg);
        }
    }

    private void generateWriteRequest() {
        int fileID = (int) Math.floor(Math.random() * files.length);
        Message msg = new Message(Message.REQUEST, fileID, myNodeInfo.id, updatedTimeStamp());
        ackCounter = 0;
        fileAccessReq.get(fileID).offer(msg);
        sendMessageToAll(msg);
    }

    private void sendWriteRequest(int fileID) {
        NodeInfo serverInfo = serverInfos.get((int) Math.floor(serverInfos.size() * Math.random()));
        Node node = null;
        try {
            node = new Node(new Socket(serverInfo.hostname, serverInfo.port), this, serverInfo.id, myNodeInfo.id);
            node.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        Message msg = new Message(Message.WRITE, fileID, myNodeInfo.id, updatedTimeStamp());
        node.writeMsg(msg);
    }

    private void enquireServer() {
        NodeInfo serverInfo = serverInfos.get(0);
        Node node = null;
        try {
            node = new Node(new Socket(serverInfo.hostname, serverInfo.port), this, serverInfo.id, myNodeInfo.id);
            node.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        node.writeMsg(new Message(Message.ENQUIRE, myNodeInfo.id, updatedTimeStamp()));
    }

    private void processQueue(int fileID) {
        Message message = fileAccessReq.get(fileID).peek();
        if (message != null) {
            if (message.getClientID() == myNodeInfo.id) {
                sendWriteRequest(fileID);
            } else {
                clients.get(message.getClientID()).writeMsg(new Message(Message.ACKNOWLEDGEMENT, fileID, myNodeInfo.id, updatedTimeStamp()));
            }
        }
    }

    @Override
    synchronized public boolean processMessage(Message msg, Node node) {
        updatedTimeStamp(msg.getTimeStamp());
//        System.err.println("type = " + msg.getType() + " fid = " + msg.getFileID() + " id = " + msg.getClientID() + " ts = " + msg.getTimeStamp());
        switch (msg.getType()) {
            case Message.REQUEST:
                recievedRequest(msg, node);
                return true;
            case Message.RELEASE:
                recievedRelease(msg);
                return true;
            case Message.ACKNOWLEDGEMENT:
                recievedAcknowledgement(msg);
                return true;
            case Message.WRITE_REPLY:
                recievedWriteReply(msg);
                return false;
            case Message.READ_REPLY:
                recievedReadReply(msg);
                return false;
            case Message.ENQUIRE_REPLY:
                recievedEnquireReply(msg);
                return false;
            case GENERATE_REQUEST:
                generateWriteRequest();
        }
        return false;
    }
}
