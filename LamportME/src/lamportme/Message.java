/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lamportme;

import java.io.*;

/**
 *
 * @author Sompa
 */
public class Message implements Serializable, Comparable<Message> {

    private int timeStamp;
    private int clientID;
    private int fileID;
    private String[] files = null;
    private String message = null;
    private int type;
    public static final int REQUEST = 1;
    public static final int ACKNOWLEDGEMENT = 2;
    public static final int RELEASE = 3;
    public static final int ENQUIRE = 4;
    public static final int ENQUIRE_REPLY = 5;
    public static final int WRITE = 6;
    public static final int WRITE_REPLY = 7;
    public static final int READ = 8;
    public static final int READ_REPLY = 9;
    public static final int REWRITE = 10;

    public Message(int type, int clientID, int timestamp) {
        this.clientID = clientID;
        this.type = type;
        this.timeStamp = timestamp;
    }

    public Message(int type, int fileID, int clientID, int timestamp) {
        this.type = type;
        this.fileID = fileID;
        this.clientID = clientID;
        this.timeStamp = timestamp;
    }

    public Message(File[] files) {
        this.type = ENQUIRE_REPLY;
        this.files = new String[files.length];
        for (int i = 0; i < this.files.length; i++) {
            this.files[i] = files[i].getName();
        }
    }

    public Message(String message) {
        this.type = READ_REPLY;
        this.message = message;
    }

    /**
     * @return the timeStamp
     */
    public int getTimeStamp() {
        return timeStamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @return the clientID
     */
    public int getClientID() {
        return clientID;
    }

    /**
     * @param clientID the clientID to set
     */
    public void setClientID(int clientID) {
        this.clientID = clientID;
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
     * @return the files
     */
    public String[] getFiles() {
        return files;
    }

    /**
     * @param files the files to set
     */
    public void setFiles(String[] files) {
        this.files = files;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int compareTo(Message t) {
        int val = (new Integer(timeStamp).compareTo(t.getTimeStamp()));
        return (val == 0) ? (new Integer(clientID).compareTo(t.clientID)) : val;
    }
}
