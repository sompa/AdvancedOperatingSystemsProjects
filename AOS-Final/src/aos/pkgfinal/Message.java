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
import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable {

    private int headTimeStamp;
    private int UUID;
    private int headID;
    private int id;
    private int localServerTimeStamp;
    private int timeStamp;
    private int fileID;
    private String[] files = null;
    private String message = null;
    private int type;
    private RequestList list;
    private int commitSeqNo;
    public static final int READ_REQUEST = 0;
    public static final int WRITE_REQUEST = 1;
    public static final int ACKNOWLEDGEMENT = 2;
    public static final int PROCESS_WRITE = 3;
    public static final int ENQUIRE = 4;
    public static final int ENQUIRE_REPLY = 5;
    public static final int WRITE = 6;
    public static final int WRITE_REPLY = 7;
    public static final int READ = 8;
    public static final int READ_REPLY = 9;
    public static final int COMMIT_SUCCESS = 10;
    public static final int GET_UPDATE = 11;
    public static final int SET_UPDATE = 12;
    public static final int FAILED = 13;
    public static final int COMMIT_FAIL = 14;
    public static final int ECHO = 15;
    public static final int NEW_TAIL = 16;
    public static final int NEW_HEAD = 17;
    public static final int MY_LIST = 18;
    

    public Message(int type) {
        this.type = type;
    }

    public Message(int type, int serverID, int headID, int UUID, int fileID, int headTimeStamp, int localServerTimeStamp, Map<Integer, Node> clients) {
        this.type = type;   // msg sent from the head server to the other server OR msg sent by the other servers to the tail server
        this.id = serverID;
        this.headID = headID;
        this.UUID = UUID;
        this.fileID = fileID;
        this.headTimeStamp = headTimeStamp;
        this.localServerTimeStamp = localServerTimeStamp; // this value is null when the head sends the message
    }

    public Message(int type, int clientID, int fileID) { // msg sent from the client to the server contains MSG_TYPE(R/W),the id of the client that is requesting, the file to be R/W
        this.type = type;
        this.id = clientID;
        this.fileID = fileID;
    }
    
    public Message(int type, int id, int headID, RequestList list) {
        this.type = type;
        this.id = id;
        this.headID = headID;
        this.list = list;
    }
//    public Message(int type){//message sent by tail to its predecessor COMMIT notification by the successor
//    }

    public Message(int type, int clientID) { // msg sent from the head server to requesting client
        this.type = type;
        this.id = clientID;
    }

    public Message(int type, Request request) {
        this.type = type;
        this.headID = request.getHeadID();
        this.localServerTimeStamp = request.getLocalServerTimeStamp();
        this.fileID = request.getFileID();
        this.headTimeStamp = request.getHeadValue();
        this.UUID = request.getUUID();
        this.id = request.getId();
    }
    
    public Message(File[] files) {
        this.type = ENQUIRE_REPLY;
        this.files = new String[files.length];
        for (int i = 0; i < this.files.length; i++) {
            this.files[i] = files[i].getName();
        }
    }

    public Message(int type, int id, RequestList list) {
        this.type = type;
        this.id = id;
        this.list = list;
    }
    
    public Message(String message) {
        this.message = message;
    }

    public int getHeadTimeStamp() {
        return headTimeStamp;
    }

    public int getHeadID() {
        return headID;
    }

    public void setHeadID(int headID) {
        this.headID = headID;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setUUID(int UUID) {
        this.UUID = UUID;
    }

    public int getUUID() {
        return UUID;
    }

    public void setHeadTimeStamp(int headTimeStamp) {
        this.headTimeStamp = headTimeStamp;
    }

    /**
     * @return the clientID
     */
    public int getID() {
        return id;
    }

    /**
     * @param clientID the clientID to set
     */
    public void setID(int id) {
        this.id = id;
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

    public void setlocalServerTimeStamp(int localServerTimeStamp) {
        this.localServerTimeStamp = localServerTimeStamp;
    }

    public int getLocalServerTimeStamp() {
        return localServerTimeStamp;
    }

    /**
     * @return the list
     */
    public RequestList getList() {
        return list;
    }

    /**
     * @param list the list to set
     */
    public void setList(RequestList list) {
        this.list = list;
    }

    /**
     * @return the commitSeqNo
     */
    public int getCommitSeqNo() {
        return commitSeqNo;
    }

    /**
     * @param commitSeqNo the commitSeqNo to set
     */
    public void setCommitSeqNo(int commitSeqNo) {
        this.commitSeqNo = commitSeqNo;
    }
}
