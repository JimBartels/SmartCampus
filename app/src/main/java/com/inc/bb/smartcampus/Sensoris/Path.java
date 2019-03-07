
package com.inc.bb.smartcampus.Sensoris;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Path {

    @SerializedName("positionEstimate")
    @Expose
    private List<PositionEstimate> positionEstimate = null;

    public List<PositionEstimate> getPositionEstimate() {
        return positionEstimate;
    }

    public void setPositionEstimate(List<PositionEstimate> positionEstimate) {
        this.positionEstimate = positionEstimate;
    }

}
