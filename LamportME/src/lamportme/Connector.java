/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lamportme;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sompa
 */
public class Connector {
    Map<Integer, Node> nodes = new HashMap<Integer, Node>();
    List<NodeInfo> nodeInfos = null;
    NodeInfo myNodeInfo = null;
    NodeHandler handler;
    
    public Connector(List<NodeInfo> nodeInfos, NodeInfo myNodeInfo, NodeHandler handler) {
        this.myNodeInfo = myNodeInfo;
        this.nodeInfos = nodeInfos;
        this.handler = handler;
    }
    
    public Map<Integer, Node> makeAllConnections() {
        connectToAllAbove();
        waitForConnectionsFromBelow();
        System.err.println("nodes : " + nodes.size());
        return nodes;
    }

    private void waitForConnectionsFromBelow() {
        try {
            ServerSocket server = new ServerSocket(myNodeInfo.port);
            for(int i=myNodeInfo.id+1;i<nodeInfos.size();i++) {
                try {
                    Socket socket = server.accept();
                    Node node = new Node(socket, handler);
                    nodes.put(node.id, node);
                    System.err.println("accepted to node: " + node.id);
                } catch (IOException ex) {
                    Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            server.close();
        } catch (IOException ex) {
            Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void connectToAllAbove() {
        for(int i=0;i<myNodeInfo.id;i++) {
            try {
                NodeInfo serverInfo = nodeInfos.get(i);
                Socket socket = new Socket(serverInfo.hostname, serverInfo.port);
                Node node = new Node(socket, handler, serverInfo.id, myNodeInfo.id);
                nodes.put(serverInfo.id, node);
                System.err.println("connected to node: " + node.id);
            } catch (UnknownHostException ex) {
                Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
