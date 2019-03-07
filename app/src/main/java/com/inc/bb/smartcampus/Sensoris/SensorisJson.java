
package com.inc.bb.smartcampus.Sensoris;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SensorisJson {

    @SerializedName("message")
    @Expose
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

}
