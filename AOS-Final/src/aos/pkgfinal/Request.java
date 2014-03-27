/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aos.pkgfinal;

import java.io.Serializable;

/**
 *
 * @author Sompa
 */
public class Request implements Serializable {
    final private static int MAX_SERVERS = 10;
    private static int serverID = 0;

    /**
     * @return the serverID
     */
    public static int getServerID() {
        return serverID;
    }

    /**
     * @param aServerID the serverID to set
     */
    public static void setServerID(int aServerID) {
        serverID = aServerID;
    }
    
    private int headValue;
    private int[] clockValues = new int[MAX_SERVERS];
    private boolean isConcurrent = false;
    private int UUID;
    private int fileID;
    private int headID;
    private int id;
    
    public Request(Message msg) {
        clockValues[serverID] = 1;
        updateRequest(msg);
    }
    
    public void updateRequest(Message msg) {
        this.headID = msg.getHeadID();
        this.UUID = msg.getUUID();
        this.fileID = msg.getFileID();
        clockValues[msg.getHeadID()] = msg.getHeadTimeStamp();
        clockValues[msg.getID()] = msg.getLocalServerTimeStamp();
        headValue = msg.getHeadTimeStamp();
        id = msg.getID();
    }
    
    boolean isFull(int n) {
        for(int i=0;i<clockValues.length;i++) {
            if(clockValues[i] != 0)
                n--;
            System.out.print(" " + clockValues[i]);
        }
        System.out.println("value of n  " + n);
        return n==0;
    }
    
    boolean isConcurrent(Request request) {
        int flag = 0, unflag;
        for(int i=0;i<clockValues.length;i++) {
            unflag = flag;
            if(clockValues[i] > request.clockValues[i])
                flag = 1;
            else if(clockValues[i] < request.clockValues[i])
                flag = -1;
            else
                flag = 0;
            if(flag != unflag && Math.abs(flag) == Math.abs(unflag))
                return true;
        }
        return false;
    }
    
    public int getLocalServerTimeStamp() {
        return clockValues[this.id];
    }
    /**
     * @return the headValue
     */
    public int getHeadValue() {
        return headValue;
    }

    /**
     * @param headValue the headValue to set
     */
    public void setHeadValue(int headValue) {
        this.headValue = headValue;
    }

    /**
     * @return the clockValues
     */
    public int[] getClockValues() {
        return clockValues;
    }

    /**
     * @param clockValues the clockValues to set
     */
    public void setClockValues(int[] clockValues) {
        this.clockValues = clockValues;
    }

    /**
     * @return the isConcurrent
     */
    public boolean isConcurrent() {
        return isConcurrent;
    }

    /**
     * @param isConcurrent the isConcurrent to set
     */
    public void setIsConcurrent(boolean isConcurrent) {
        this.isConcurrent = isConcurrent;
    }

    /**
     * @return the UUID
     */
    public int getUUID() {
        return UUID;
    }

    /**
     * @param UUID the UUID to set
     */
    public void setUUID(int UUID) {
        this.UUID = UUID;
    }

    /**
     * @return the fileID
     */
    public int getFileID() {
        return fileID;
    }

    /**
     * @param fileID the fileID to set
     */
    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    /**
     * @return the headID
     */
    public int getHeadID() {
        return headID;
    }

    /**
     * @param headID the headID to set
     */
    public void setHeadID(int headID) {
        this.headID = headID;
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
