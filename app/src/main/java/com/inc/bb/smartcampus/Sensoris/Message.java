
package com.inc.bb.smartcampus.Sensoris;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Message {

    @SerializedName("path")
    @Expose
    private Path path;
    @SerializedName("envelope")
    @Expose
    private Envelope envelope;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public void setEnvelope(Envelope envelope) {
        this.envelope = envelope;
    }

}
