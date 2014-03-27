/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aos.pkgfinal;

/**
 *
 * @author Sompa
 */
import java.io.*;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sompa
 */
public class ServerInstance extends Thread implements NodeHandler {

    NodeInfo myServerNodeInfo;
    NodeInfo myPredecessorInfo;
    NodeInfo tailServer;
    List<NodeInfo> serverInfos;
    Map<Integer, Node> servers;
    Map<Integer, Node> clients;
    String homeFolder;
    File[] files;
    static int timestamp;
    Integer[] vectorTimeStamp;
    int reqSeqNo = 1;
    static int commitSeqNo;
//    Map<Integer, List<Integer>> fileRequests = new HashMap<Integer, List<Integer>>();
//    Map<Integer, Request> requests = new HashMap<Integer, Request>();
    RequestList ackedRequests = new RequestList();
    RequestList forwardedRequests = new RequestList();

    private ServerInstance(String[] args) {
        homeFolder = args[0];
        NodeInfo masterServerInfo = new NodeInfo();
        serverInfos = NodeInfo.readNodeInfo(args[1], masterServerInfo);
        myServerNodeInfo = serverInfos.get(Integer.parseInt(args[2]));
        tailServer = serverInfos.get(serverInfos.size() - 1);
        myPredecessorInfo = (myServerNodeInfo.getId() == 0) ? null : serverInfos.get(Integer.parseInt(args[2]) - 1);
        Connector serverConnector = new Connector(serverInfos, myServerNodeInfo, masterServerInfo, this);
        servers = serverConnector.makeAllConnections();
        clients = new HashMap<>();
        files = new File(homeFolder).listFiles();
        Request.setServerID(myServerNodeInfo.getId());
        System.out.println("All servers connected");
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("USAGE ServerInstance HomeFolder ServerFile ServerID");
            //return;
        }
        new ServerInstance(args).process();

    }

    private void process() {
        print();
        startProcessingServerReq();
        startReadingClientMessages();
    }

    @Override
    synchronized public boolean processMessage(Message msg, Node node) {
        updatedTimeStamp(msg.getTimeStamp());
        switch (msg.getType()) {
            case Message.ENQUIRE:
                recievedEnquire(msg, node);
                return false;
            case Message.READ:
                recievedReadReq(msg, node);
                break;
            case Message.WRITE:
                recievedWriteReq(msg, node);
                break;
            case Message.WRITE_REQUEST:
                processWriteReq(msg);
                break;
            case Message.PROCESS_WRITE:
                processingReqAtTail(msg);
                break;
            case Message.GET_UPDATE:
                sendUpdateForReadReq(msg);
                break;
            case Message.READ_REPLY:
                sendReplyToClient(msg);
                break;
            case Message.COMMIT_SUCCESS:
                writeToFile(msg);
            case Message.COMMIT_FAIL:
                writeToPredecessor(msg);
                break;
            case Message.ECHO:
                node.writeMsg(msg);
                break;
            case Message.FAILED:
                handleFailed(msg);
                break;
            case Message.NEW_TAIL:
                handleNewTail(msg, node);
                break;
            case Message.MY_LIST:
                handleForwardedList(msg);
                break;
            case Message.NEW_HEAD:
                handleNewHead(msg);
        }
        return true;
    }

    private void startReadingClientMessages() {
        System.err.println("start processing client message: ");
        try {
            ServerSocket serverSocket = new ServerSocket(myServerNodeInfo.getPort());
            while (true) {
                new Node(serverSocket.accept(), this).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerInstance.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startProcessingServerReq() {
        System.err.println("start reading processing server message: ");
        for (Node node : servers.values()) {
            node.start();
        }
    }

    void recievedEnquire(Message msg, Node node) {
        System.err.println("Enquire msg: " + msg.getID() + " " + node.id);
        Message reply = new Message(files);
        node.writeMsg(reply);
    }

    private void recievedReadReq(Message msg, Node node) {
        msg.setID(node.id);
        msg.setHeadID(node.id);
        msg.setHeadTimeStamp(updatedTimeStamp());
        msg.setlocalServerTimeStamp(updatedTimeStamp());
        System.out.println("read msg recd");
        if (forwardedRequests.hasRequestForFileID(msg.getFileID()) && this.myServerNodeInfo.getId() != this.tailServer.getId()) {//(localQueue.contains(msg)) {
            saveClientRequest(msg, node);
            queryTail(msg);
        } else {
            sendReadAck(msg, node);
        }
    }

    synchronized private void recievedWriteReq(Message msg, Node node) {
        saveClientRequest(msg, node);
        msg.setID(myServerNodeInfo.getId());
        msg.setHeadID(myServerNodeInfo.getId());
        msg.setHeadTimeStamp(updatedTimeStamp());
        msg.setlocalServerTimeStamp(updatedTimeStamp());
        forwardedRequests.add(new Request(msg));
        msg.setType(Message.WRITE_REQUEST);
        System.out.println("write uuid " + msg.getUUID());
        for (Node intermediateNode : servers.values()) {
            if (intermediateNode.id != myServerNodeInfo.getId() && intermediateNode.id != tailServer.getId()) {
                intermediateNode.writeMsg(msg);
            }
        }
    }

    private void processWriteReq(Message msg) {
        forwardedRequests.add(new Request(msg));
        System.out.println("write uuid " + msg.getUUID() + " head " + msg.getHeadID());
        msg.setlocalServerTimeStamp(updatedTimeStamp());
        msg.setID(myServerNodeInfo.getId());
        msg.setType(Message.PROCESS_WRITE);
        servers.get(tailServer.getId()).writeMsg(msg);
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

    int updatedTimeStamp() {
        timestamp += myServerNodeInfo.getId() + 1;
        return timestamp;
    }

    void updatedTimeStamp(int timestamp) {
        ServerInstance.timestamp = (ServerInstance.timestamp > timestamp) ? ServerInstance.timestamp : timestamp;
        updatedTimeStamp();
    }

    private void processingReqAtTail(Message msg) {
        Request request = ackedRequests.updateOrAddRequest(msg);
        System.out.println("write uuid " + msg.getUUID() + " size no zero " + serverInfos.size() + " head " + msg.getHeadID() + " svr " + msg.getID() + " isfull " + request.isFull(serverInfos.size()));
        if (request.isFull(serverInfos.size())) {
            msg.setType(Message.COMMIT_SUCCESS);
            msg.setCommitSeqNo(++commitSeqNo);
            writeToFile(msg);
            ackedRequests.removeWithUUID(msg.getUUID());
            servers.get(myPredecessorInfo.getId()).writeMsg(msg);
            if (myServerNodeInfo.getId() == msg.getHeadID()) {
                sendReplyToClient(msg);
            }
        }
    }

    private void writeToPredecessor(Message msg) {
        System.err.println("ReWrite msg: " + msg.getID() + " head " + msg.getHeadID());
        if (myPredecessorInfo != null) {
            servers.get(myPredecessorInfo.getId()).writeMsg(msg);
        }
        if (myServerNodeInfo.getId() == msg.getHeadID()) {
            sendReplyToClient(msg);
        }
        forwardedRequests.removeWithUUID(msg.getUUID());
    }

    private void writeToFile(Message msg) {
        System.err.println("Write file: " + msg.getID() + " ");
        try (PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(files[msg.getFileID()], true)))) {
            out.println(msg.getID() + "," + msg.getUUID() + "," + msg.getLocalServerTimeStamp()+","+msg.getCommitSeqNo());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerInstance.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void sendReadAck(Message msg, Node node) {
        System.err.println("Read msg: " + msg.getID());
        String replyMsg = readLastLine(files[msg.getFileID()]);
        node.writeMsg(new Message(replyMsg + " message read from" + msg.getID()));
    }

    private void queryTail(Message msg) {
        System.out.println("send read msg to tail server");
        msg.setType(Message.GET_UPDATE);
        msg.setHeadID(myServerNodeInfo.getId());
        msg.setlocalServerTimeStamp(updatedTimeStamp());
        msg.setHeadTimeStamp(msg.getLocalServerTimeStamp());
        servers.get(tailServer.getId()).writeMsg(msg);
    }

    private void sendUpdateForReadReq(Message msg) {
        msg.setType(Message.READ_REPLY);
        System.err.println("Read msg: " + msg.getHeadID());
        String replyMsg = readLastLine(files[msg.getFileID()]);
        msg.setMessage(replyMsg + " message read from" + msg.getID());
        servers.get(msg.getHeadID()).writeMsg(msg);
    }

    private void sendReplyToClient(Message msg) {
        clients.get(msg.getUUID()).writeMsg(msg);
        clients.remove(msg.getUUID());
    }

    private void saveClientRequest(Message msg, Node node) {
        msg.setUUID(myServerNodeInfo.getId() * 100000 + (reqSeqNo++));
        clients.put(msg.getUUID(), node);
    }

    private void print() {
        System.out.println("Tail: " + this.tailServer.getId());
        if (this.myPredecessorInfo != null) {
            System.out.println("Pred: " + this.myPredecessorInfo.getId());
        }
        System.out.println("Me: " + this.myServerNodeInfo.getId());
        for (NodeInfo node : serverInfos) {
            System.out.println("server: " + node.getId());
        }
        for (Integer node : servers.keySet()) {
            System.out.println("conn server: " + node);
        }
    }

    private void handleFailed(Message msg) {
        int id = msg.getID();
        serverInfos.remove(nodeInfoByID(id));
        servers.remove(id);
        if (id == tailServer.getId()) {
            if (id == myServerNodeInfo.getId() + 1) {
                sendMsgToAll(new Message(Message.NEW_TAIL, myServerNodeInfo.getId()));
            }
            tailServer = serverInfos.get(serverInfos.size() - 1);
        } else {

            if (myPredecessorInfo != null && id == myPredecessorInfo.getId()) {
                myPredecessorInfo = null;
                int newId = id - 1;
                while (myPredecessorInfo == null && newId != -1) {
                    myPredecessorInfo = this.nodeInfoByID(newId);
                    newId--;
                }
                System.out.println("pred set: " + myPredecessorInfo + " newId " + newId);
            }
            if (myServerNodeInfo.getId() == serverInfos.get(0).getId()) {
                sendMsgToAll(new Message(Message.NEW_HEAD, id, myServerNodeInfo.getId(), new RequestList(forwardedRequests.getAndSetAllHeadRequests(id, myServerNodeInfo.getId()))));
            }
        }
    }

    private void sendMsgToAll(Message message) {
        for (Node n : servers.values()) {
            n.writeMsg(message);
        }
        System.out.println("Notification sent to every other server node");
    }

    private void handleNewTail(Message msg, Node node) {
        tailServer = nodeInfoByID(msg.getID());
        node.writeMsg(new Message(Message.MY_LIST, myServerNodeInfo.getId(), forwardedRequests));
    }

    private NodeInfo nodeInfoByID(int id) {
        for (NodeInfo nodeInfo : serverInfos) {
            if (nodeInfo.getId() == id) {
                return nodeInfo;
            }
        }
        return null;
    }

    private void handleForwardedList(Message msg) {
        for (Request request : msg.getList().getRequests()) {
            processingReqAtTail(new Message(Message.PROCESS_WRITE, request));
        }
    }

    private void handleNewHead(Message msg) {
        forwardedRequests.removeAllWithHeadID(msg.getID());
        forwardedRequests.insertRequests(msg.getList());
    }
}
