/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aos.pkgfinal;

/**
 *
 * @author Sompa
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sompa
 */
public class NodeInfo {

    private int port;
    private String hostName;
    private int id;

    public NodeInfo() {
    }

    public NodeInfo(int port, String hostName, int id) {
        setInfo(port, hostName, id);
    }

    public void setInfo(int port, String hostName, int id) {
        this.port = port;
        this.hostName = hostName;
        this.id = id;
    }

    /**
     *
     * @param filename FILE FORMAT 
     * masterPort masterHostname masterId
     * port hostName id
     * port hostName id . . . .
     *
     * @return
     */
    public static List<NodeInfo> readNodeInfo(String fileName) {
        return readNodeInfo(fileName, new NodeInfo());
    }
    
    public static List<NodeInfo> readNodeInfo(String fileName, NodeInfo masterServerInfo) {
        List<NodeInfo> nodeInfos = new ArrayList<>();
        try {
            try (Scanner scr = new Scanner(new File(fileName))) {
                masterServerInfo.setInfo(scr.nextInt(), scr.next(), scr.nextInt());
                while (scr.hasNext()) {
                    nodeInfos.add(new NodeInfo(scr.nextInt(), scr.next(), scr.nextInt()));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NodeInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeInfos;

    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
}
