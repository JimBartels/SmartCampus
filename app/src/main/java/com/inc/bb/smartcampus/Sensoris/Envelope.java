
package com.inc.bb.smartcampus.Sensoris;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Envelope {

    @SerializedName("submitter")
    @Expose
    private String submitter;
    @SerializedName("transientVehicleID")
    @Expose
    private Integer transientVehicleID;
    @SerializedName("generated_TimeStampUTC_ms")
    @Expose
    private Integer generatedTimeStampUTCMs;
    @SerializedName("vehicleProfileID")
    @Expose
    private Integer vehicleProfileID;
    @SerializedName("vehicleMetaData")
    @Expose
    private VehicleMetaData vehicleMetaData;
    @SerializedName("version")
    @Expose
    private String version;

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public Integer getTransientVehicleID() {
        return transientVehicleID;
    }

    public void setTransientVehicleID(Integer transientVehicleID) {
        this.transientVehicleID = transientVehicleID;
    }

    public Integer getGeneratedTimeStampUTCMs() {
        return generatedTimeStampUTCMs;
    }

    public void setGeneratedTimeStampUTCMs(Integer generatedTimeStampUTCMs) {
        this.generatedTimeStampUTCMs = generatedTimeStampUTCMs;
    }

    public Integer getVehicleProfileID() {
        return vehicleProfileID;
    }

    public void setVehicleProfileID(Integer vehicleProfileID) {
        this.vehicleProfileID = vehicleProfileID;
    }

    public VehicleMetaData getVehicleMetaData() {
        return vehicleMetaData;
    }

    public void setVehicleMetaData(VehicleMetaData vehicleMetaData) {
        this.vehicleMetaData = vehicleMetaData;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
