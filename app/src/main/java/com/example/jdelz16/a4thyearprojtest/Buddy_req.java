package com.example.jdelz16.a4thyearprojtest;

/**
 * Created by jdelz16 on 15/02/2018.
 */

public class Buddy_req {
    public String requestType;
    public String uniqueIdentifier;
    public String receiverReply;
    public double timer;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getReceiverReply() {
        return receiverReply;
    }

    public void setReceiverReply(String receiverReply) {
        this.receiverReply = receiverReply;
    }

    public double getTimer() {
        return timer;
    }

    public void setTimer(double timer) {
        this.timer = timer;
    }
}
