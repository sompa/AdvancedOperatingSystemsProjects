/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lamportme;

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

    int port;
    String hostname;
    int id;

    public NodeInfo(int id, String hostname, int port) {
        this.id = id;
        this.hostname = hostname;
        this.port = port;
    }

    
    /**
     * 
     * @param filename
     * FILE FORMAT
     * id hostname port
     * id hostname port
     * .
     * .
     * .
     * .
     * 
     * @return 
     */
    public static List<NodeInfo> readNodeInfos(String filename) {
        List<NodeInfo> nodeInfos = new ArrayList<NodeInfo>();
        Scanner scr = null;
        try {
            scr = new Scanner(new File(filename));
            while (scr.hasNext()) {
                nodeInfos.add(new NodeInfo(scr.nextInt(), scr.next(), scr.nextInt()));
            }
            scr.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NodeInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeInfos;
    }
}
