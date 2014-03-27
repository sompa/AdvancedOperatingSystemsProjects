/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aos.pkgfinal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sompa
 */
public class RequestList implements Serializable {

    private List<Request> requests;

    public RequestList() {
        requests = new ArrayList<>();
    }

    public RequestList(List<Request> requests) {
        this.requests = requests;
    }

    public void add(Request request) {
        requests.add(request);
    }

    public int getUUIDPosition(int UUID) {
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getUUID() == UUID) {
                return i;
            }
        }
        return -1;
    }

    public boolean removeWithUUID(int UUID) {
        int pos = getUUIDPosition(UUID);
        if (pos > -1) {
            requests.remove(pos);
        }
        return pos > -1;
    }

    /**
     * @return the requests
     */
    public List<Request> getRequests() {
        return requests;
    }

    public Request updateOrAddRequest(Message msg) {
        int pos = getUUIDPosition(msg.getUUID());
        Request request = new Request(msg);
        if (pos > -1) {
            request = requests.get(pos);
            request.updateRequest(msg);
        } else {
            requests.add(request);
        }
        return request;
    }

    public boolean hasRequestForFileID(int fileID) {
        for (Request request : requests) {
            if (request.getFileID() == fileID) {
                return true;
            }
        }
        return false;
    }

    void removeAllWithHeadID(int headID) {
        this.requests.removeAll(getAndSetAllHeadRequests(headID, headID)); 
    }

    List<Request> getAndSetAllHeadRequests(int oldID, int headID) {
        List<Request> reqs = new ArrayList<>();
        for(Request req: this.requests) {
            if(oldID == req.getHeadID()) {
                req.setHeadID(headID);
                reqs.add(req);
            }
        }
        return reqs;
    }

    void insertRequests(RequestList list) {
        for (Request request : list.getRequests()) {
            requests.add(request);
        }
    }
}
