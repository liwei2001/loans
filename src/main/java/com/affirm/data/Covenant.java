package com.affirm.data;

public class Covenant {

    private int bankId;
    private int facilityId;
    private float maxDefaultRate;
    private String bannedState;

    public Covenant(int bankId, int facilityId, float maxDefaultRate, String bannedState) {
        this.bankId = bankId;
        this.facilityId = facilityId;
        this.maxDefaultRate = maxDefaultRate;
        this.bannedState = bannedState;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }

    public float getMaxDefaultRate() {
        return maxDefaultRate;
    }

    public void setMaxDefaultRate(float maxDefaultRate) {
        this.maxDefaultRate = maxDefaultRate;
    }

    public String getBannedState() {
        return bannedState;
    }

    public void setBannedState(String bannedState) {
        this.bannedState = bannedState;
    }
}
