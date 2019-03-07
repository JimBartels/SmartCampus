
package com.inc.bb.smartcampus.Sensoris;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PositionEstimate {

    @SerializedName("headingAccuracy_deg")
    @Expose
    private Integer headingAccuracyDeg;
    @SerializedName("headingDetectionType")
    @Expose
    private String headingDetectionType;
    @SerializedName("positionType")
    @Expose
    private String positionType;
    @SerializedName("longitude_deg")
    @Expose
    private Double longitudeDeg;
    @SerializedName("speed_mps")
    @Expose
    private Double speedMps;
    @SerializedName("latitude_deg")
    @Expose
    private Double latitudeDeg;
    @SerializedName("interpolatePoint")
    @Expose
    private Boolean interpolatePoint;
    @SerializedName("heading_deg")
    @Expose
    private Double headingDeg;
    @SerializedName("altitudeAccuracy_m")
    @Expose
    private Integer altitudeAccuracyM;
    @SerializedName("altitude_m")
    @Expose
    private Double altitudeM;
    @SerializedName("speedAccuracy_mps")
    @Expose
    private Integer speedAccuracyMps;
    @SerializedName("timeStampUTC_ms")
    @Expose
    private Long timeStampUTCMs;
    @SerializedName("horizontalAccuracy_m")
    @Expose
    private Integer horizontalAccuracyM;
    @SerializedName("currentLaneEstimation")
    @Expose
    private Integer currentLaneEstimation;
    @SerializedName("speedDetectionType")
    @Expose
    private String speedDetectionType;

    public Integer getHeadingAccuracyDeg() {
        return headingAccuracyDeg;
    }

    public void setHeadingAccuracyDeg(Integer headingAccuracyDeg) {
        this.headingAccuracyDeg = headingAccuracyDeg;
    }

    public String getHeadingDetectionType() {
        return headingDetectionType;
    }

    public void setHeadingDetectionType(String headingDetectionType) {
        this.headingDetectionType = headingDetectionType;
    }

    public String getPositionType() {
        return positionType;
    }

    public void setPositionType(String positionType) {
        this.positionType = positionType;
    }

    public Double getLongitudeDeg() {
        return longitudeDeg;
    }

    public void setLongitudeDeg(Double longitudeDeg) {
        this.longitudeDeg = longitudeDeg;
    }

    public Double getSpeedMps() {
        return speedMps;
    }

    public void setSpeedMps(Double speedMps) {
        this.speedMps = speedMps;
    }

    public Double getLatitudeDeg() {
        return latitudeDeg;
    }

    public void setLatitudeDeg(Double latitudeDeg) {
        this.latitudeDeg = latitudeDeg;
    }

    public Boolean getInterpolatePoint() {
        return interpolatePoint;
    }

    public void setInterpolatePoint(Boolean interpolatePoint) {
        this.interpolatePoint = interpolatePoint;
    }

    public Double getHeadingDeg() {
        return headingDeg;
    }

    public void setHeadingDeg(Double headingDeg) {
        this.headingDeg = headingDeg;
    }

    public Integer getAltitudeAccuracyM() {
        return altitudeAccuracyM;
    }

    public void setAltitudeAccuracyM(Integer altitudeAccuracyM) {
        this.altitudeAccuracyM = altitudeAccuracyM;
    }

    public Double getAltitudeM() {
        return altitudeM;
    }

    public void setAltitudeM(Double altitudeM) {
        this.altitudeM = altitudeM;
    }

    public Integer getSpeedAccuracyMps() {
        return speedAccuracyMps;
    }

    public void setSpeedAccuracyMps(Integer speedAccuracyMps) {
        this.speedAccuracyMps = speedAccuracyMps;
    }

    public Long getTimeStampUTCMs() {
        return timeStampUTCMs;
    }

    public void setTimeStampUTCMs(Long timeStampUTCMs) {
        this.timeStampUTCMs = timeStampUTCMs;
    }

    public Integer getHorizontalAccuracyM() {
        return horizontalAccuracyM;
    }

    public void setHorizontalAccuracyM(Integer horizontalAccuracyM) {
        this.horizontalAccuracyM = horizontalAccuracyM;
    }

    public Integer getCurrentLaneEstimation() {
        return currentLaneEstimation;
    }

    public void setCurrentLaneEstimation(Integer currentLaneEstimation) {
        this.currentLaneEstimation = currentLaneEstimation;
    }

    public String getSpeedDetectionType() {
        return speedDetectionType;
    }

    public void setSpeedDetectionType(String speedDetectionType) {
        this.speedDetectionType = speedDetectionType;
    }

}
