/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aos.pkgfinal;

/**
 *
 * @author Sompa
 */
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sompa
 */
public class ClientInstance implements NodeHandler {

    NodeInfo myClientInfo;
    List<NodeInfo> serverInfos;
    List<NodeInfo> clientInfos;
    String[] files;
    final private int GENERATE_REQUEST = -1;

    public ClientInstance(String[] args) {
        serverInfos = NodeInfo.readNodeInfo(args[0]);
        clientInfos = NodeInfo.readNodeInfo(args[1]);
        myClientInfo = clientInfos.get(Integer.parseInt(args[2]));

    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("USAGE ClientInstance ServerFile ClientFile ClientID");
            return;
        }
        new ClientInstance(args).process();
    }

    private void enquireServer() {
        NodeInfo serverInfo = serverInfos.get(0);
        Node node = null;
        try {
            node = new Node(new Socket(serverInfo.getHostName(), serverInfo.getPort()), this, serverInfo.getId(), myClientInfo.getId());
        } catch (UnknownHostException ex) {
            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        node.writeMsg(new Message(Message.ENQUIRE, myClientInfo.getId()));
        node.process();
    }

    synchronized void recievedEnquireReply(Message msg) {
        this.files = msg.getFiles();
        System.out.println("files: " + this.files.length);
        for (int i = 0; i < files.length; i++) {
            System.out.println("file[" + i + "] = " + files[i]);
        }
    }

    private void generateRequest() {
        int fileID = (int) Math.floor(Math.random() * files.length);
        int req = (int) Math.floor(Math.random() * 2);
        if (req == Message.READ_REQUEST) {
            sendReadReqToServer(fileID);
        } else if (req == Message.WRITE_REQUEST) {
            sendWriteReqToServer(fileID);
        }
    }

    @Override
    synchronized public boolean processMessage(Message msg, Node node) {
        switch (msg.getType()) {
            case Message.ENQUIRE_REPLY:
                recievedEnquireReply(msg);
                break;
            case Message.READ_REPLY:
                logReadReplies(msg);
                break;
        }
        return false;
    }

    private void sendWriteReqToServer(int fileID) {
        NodeInfo serverInfo = serverInfos.get((int) Math.floor(Math.random() * (serverInfos.size() - 1)));
        Node node = null;
        System.out.println("write req to " + serverInfo.getId());
        try {
            node = new Node(new Socket(serverInfo.getHostName(), serverInfo.getPort()), this, serverInfo.getId(), myClientInfo.getId());
        } catch (Exception ex) {
            removeNode(serverInfo.getId());
            return;
//            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
        sendMessage(Message.WRITE, fileID, serverInfo, node);
    }

    private void sendMessage(int type, int fileID, NodeInfo serverInfo, Node node) {
        Message msg = new Message(type, myClientInfo.getId(), fileID);
        msg.setFiles(files);
        msg.setID(serverInfo.getId());
        if (!(node.writeMsg(msg) && node.process())) {
            NodeInfo n = nodeInfoByID(node.id);
            System.out.println("removing node " + n.getId());
            serverInfos.remove(n);
        }
    }

    private NodeInfo nodeInfoByID(int id) {
        for (NodeInfo nodeInfo : serverInfos) {
            if (nodeInfo.getId() == id) {
                return nodeInfo;
            }
        }
        return null;
    }

    private void removeNode(int id) {
        NodeInfo n = nodeInfoByID(id);
        System.out.println("removing node " + n.getId());
        serverInfos.remove(n);
    }

    private void sendReadReqToServer(int fileID) {
        NodeInfo serverInfo = serverInfos.get((int) Math.floor(Math.random() * serverInfos.size()));
        System.out.println("generating read request to " + serverInfo.getId());
        Node node = null;
        try {
            node = new Node(new Socket(serverInfo.getHostName(), serverInfo.getPort()), this, serverInfo.getId(), myClientInfo.getId());
        } catch (Exception ex) {
            removeNode(serverInfo.getId());
            return;
        }
        sendMessage(Message.READ, fileID, serverInfo, node);
    }

    private void process() {
        enquireServer();
        try {
//            this.wait();
            Thread.sleep(5000);
//            while (true) {
            for (int i = 0; i < 100; i++) {
                generateRequest();
//                this.wait();
                Thread.sleep(300);
            }
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void logReadReplies(Message msg) {
        System.out.println("reply for read recd from " + msg.getHeadID());
        FileWriter fstream;
        try {
            fstream = new FileWriter("readlog.txt", true);
            try (BufferedWriter out = new BufferedWriter(fstream)) {
                out.write(msg.getMessage());
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientInstance.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
