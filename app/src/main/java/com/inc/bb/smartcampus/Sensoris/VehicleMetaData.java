
package com.inc.bb.smartcampus.Sensoris;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VehicleMetaData {

    @SerializedName("secondaryFuelType")
    @Expose
    private String secondaryFuelType;
    @SerializedName("primaryFuelTankVolume")
    @Expose
    private Integer primaryFuelTankVolume;
    @SerializedName("primaryFuelType")
    @Expose
    private String primaryFuelType;
    @SerializedName("vehicleTypeGenericEnum")
    @Expose
    private String vehicleTypeGenericEnum;
    @SerializedName("vehicleReferencePointDeltaAboveGround_m")
    @Expose
    private Integer vehicleReferencePointDeltaAboveGroundM;
    @SerializedName("vehicleSpecificMetadata")
    @Expose
    private VehicleSpecificMetadata vehicleSpecificMetadata;
    @SerializedName("vehicleWidth_m")
    @Expose
    private Double vehicleWidthM;
    @SerializedName("vehicleHeight_m")
    @Expose
    private Integer vehicleHeightM;
    @SerializedName("secondaryFuelTankVolume")
    @Expose
    private Integer secondaryFuelTankVolume;
    @SerializedName("vehicleLength_m")
    @Expose
    private Double vehicleLengthM;

    public String getSecondaryFuelType() {
        return secondaryFuelType;
    }

    public void setSecondaryFuelType(String secondaryFuelType) {
        this.secondaryFuelType = secondaryFuelType;
    }

    public Integer getPrimaryFuelTankVolume() {
        return primaryFuelTankVolume;
    }

    public void setPrimaryFuelTankVolume(Integer primaryFuelTankVolume) {
        this.primaryFuelTankVolume = primaryFuelTankVolume;
    }

    public String getPrimaryFuelType() {
        return primaryFuelType;
    }

    public void setPrimaryFuelType(String primaryFuelType) {
        this.primaryFuelType = primaryFuelType;
    }

    public String getVehicleTypeGenericEnum() {
        return vehicleTypeGenericEnum;
    }

    public void setVehicleTypeGenericEnum(String vehicleTypeGenericEnum) {
        this.vehicleTypeGenericEnum = vehicleTypeGenericEnum;
    }

    public Integer getVehicleReferencePointDeltaAboveGroundM() {
        return vehicleReferencePointDeltaAboveGroundM;
    }

    public void setVehicleReferencePointDeltaAboveGroundM(Integer vehicleReferencePointDeltaAboveGroundM) {
        this.vehicleReferencePointDeltaAboveGroundM = vehicleReferencePointDeltaAboveGroundM;
    }

    public VehicleSpecificMetadata getVehicleSpecificMetadata() {
        return vehicleSpecificMetadata;
    }

    public void setVehicleSpecificMetadata(VehicleSpecificMetadata vehicleSpecificMetadata) {
        this.vehicleSpecificMetadata = vehicleSpecificMetadata;
    }

    public Double getVehicleWidthM() {
        return vehicleWidthM;
    }

    public void setVehicleWidthM(Double vehicleWidthM) {
        this.vehicleWidthM = vehicleWidthM;
    }

    public Integer getVehicleHeightM() {
        return vehicleHeightM;
    }

    public void setVehicleHeightM(Integer vehicleHeightM) {
        this.vehicleHeightM = vehicleHeightM;
    }

    public Integer getSecondaryFuelTankVolume() {
        return secondaryFuelTankVolume;
    }

    public void setSecondaryFuelTankVolume(Integer secondaryFuelTankVolume) {
        this.secondaryFuelTankVolume = secondaryFuelTankVolume;
    }

    public Double getVehicleLengthM() {
        return vehicleLengthM;
    }

    public void setVehicleLengthM(Double vehicleLengthM) {
        this.vehicleLengthM = vehicleLengthM;
    }

}
