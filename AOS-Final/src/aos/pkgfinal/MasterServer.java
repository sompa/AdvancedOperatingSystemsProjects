/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aos.pkgfinal;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sompa
 */
public class MasterServer {

    List<NodeInfo> serverInfos;
    NodeInfo masterServerInfo = new NodeInfo();
    Map<Integer, Node> replicas = new HashMap<>();
    NodeHandler handler;

    public MasterServer(String[] args) {
        serverInfos = NodeInfo.readNodeInfo(args[0], masterServerInfo);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("USAGE: MasterServer serverfile ");
        }
        new MasterServer(args).process();
    }

    private void process() {
        waitForConnectionsFromAllServers();
    }

    private void waitForConnectionsFromAllServers() {
        try {
            try (ServerSocket server = new ServerSocket(masterServerInfo.getPort())) {
                System.err.println("node id " + masterServerInfo.getId());
                for (int i = masterServerInfo.getId() + 1; i < serverInfos.size(); i++) {
                    try {
                        System.out.println(" node :" + i);
                        Socket socket = server.accept();
                        Node node = new Node(socket, handler);
                        replicas.put(node.id, node);
                        System.err.println("accepted to node: " + node.id);
                    } catch (IOException ex) {
                        Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                while (!replicas.isEmpty()) {
                    echoingServers();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void echoingServers() {
        Node rNode = null;
        for (Node node : replicas.values()) {
            node.writeMsg(new Message(Message.ECHO));
            Message msg = node.readMsg();
            if (msg == null) {
                rNode = node;
                break;
            }
        }
        if (rNode!=null) {
            System.out.println("Node #" + rNode.id + " FAILED ");
            replicas.remove(rNode.id);
            for (Node n : replicas.values()) {
                n.writeMsg(new Message(Message.FAILED, rNode.id));
                System.out.println("notification sent to every other server node");
            }
        }
    }
}
