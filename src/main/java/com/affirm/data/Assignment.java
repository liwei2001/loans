package com.affirm.data;

public class Assignment {

    private int loadId;
    private int facilityId;

    public Assignment(int loadId, int facilityId) {
        this.loadId = loadId;
        this.facilityId = facilityId;
    }

    public int getLoadId() {
        return loadId;
    }

    public void setLoadId(int loadId) {
        this.loadId = loadId;
    }

    public int getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(int facilityId) {
        this.facilityId = facilityId;
    }
}
