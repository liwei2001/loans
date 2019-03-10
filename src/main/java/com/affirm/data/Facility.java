package com.affirm.data;

public class Facility {

    private int id;
    private int bankId;
    private float interestRate;
    private int loanCapacity;
    private int remainingLoanSize;
    private int currentYield;

    public Facility(int id, int bankId, float interestRate, int loanCapacity) {
        this.id = id;
        this.bankId = bankId;
        this.interestRate = interestRate;
        this.loanCapacity = loanCapacity;
        this.remainingLoanSize = loanCapacity;
        this.currentYield = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public float getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(float interestRate) {
        this.interestRate = interestRate;
    }

    public int getLoanCapacity() {
        return loanCapacity;
    }

    public void setLoanCapacity(int loanCapacity) {
        this.loanCapacity = loanCapacity;
    }

    public int getRemainingLoanSize() {
        return remainingLoanSize;
    }

    public void setRemainingLoanSize(int remainingLoanSize) {
        this.remainingLoanSize = remainingLoanSize;
    }

    public int getCurrentYield() {
        return currentYield;
    }

    public void setCurrentYield(int currentYield) {
        this.currentYield = currentYield;
    }

}
