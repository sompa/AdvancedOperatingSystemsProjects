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
    Map<Integer,Node> nodes = new HashMap<>();
    List<NodeInfo> nodeInfos = null;
    NodeInfo myNodeInfo = null;
    NodeInfo masterServerInfo = null;
    NodeHandler handler;
    
    Connector(List<NodeInfo> nodeInfos, NodeInfo myNodeInfo, NodeInfo masterServerInfo, NodeHandler handler){
        this.nodeInfos = nodeInfos;
        this.myNodeInfo = myNodeInfo;
        this.handler = handler;
        this.masterServerInfo = masterServerInfo;
    }

    public Map<Integer,Node> makeAllConnections(){
        connectToAllAbove();
        connectToMaster();
        waitForConnectionsFromBelow();
        System.err.println("nodes : " + nodes.size());
        return nodes;
    }

    private void connectToAllAbove() {
        for(int i = 0;i<myNodeInfo.getId();i++)
        {
            try{
                NodeInfo serverInfo = nodeInfos.get(i);
                Socket socket = new Socket(serverInfo.getHostName(), serverInfo.getPort());
                Node node = new Node(socket,handler, serverInfo.getId(), myNodeInfo.getId());
                nodes.put(serverInfo.getId(), node);
                System.err.println("connected to node: " + node.id);
            }catch(UnknownHostException ex){
                Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
            }catch(IOException ioe){
                Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ioe);
            }
        }
    }
   
    private void waitForConnectionsFromBelow() {
        
        try {
            try (ServerSocket server = new ServerSocket(myNodeInfo.getPort())) {
                System.err.println("node id "+ myNodeInfo.getId());
            for(int i=myNodeInfo.getId()+1;i<nodeInfos.size();i++)
            {
                try{
                    Socket socket = server.accept();
                    Node node = new Node(socket,handler);
                    nodes.put(node.id, node);
                    System.err.println("accepted to node: " + node.id);
                }catch(IOException ex){
                    Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            }
        }  catch (IOException ex) {
            Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

    private void connectToMaster() {
        try {
            Socket masterSocket = new Socket(masterServerInfo.getHostName(), masterServerInfo.getPort());
            Node masterNode = new Node(masterSocket,handler,masterServerInfo.getId(),myNodeInfo.getId());
            masterNode.start();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    
}
