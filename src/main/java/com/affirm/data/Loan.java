package com.affirm.data;

public class Loan {

    private int id;
    private int amount;
    private float interestRate;
    private float defaultRate;
    private String state;

    public Loan(int id, int amount, float interestRate, float defaultRate, String state) {
        this.id = id;
        this.amount = amount;
        this.interestRate = interestRate;
        this.defaultRate = defaultRate;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public float getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(float interestRate) {
        this.interestRate = interestRate;
    }

    public float getDefaultRate() {
        return defaultRate;
    }

    public void setDefaultRate(float defaultRate) {
        this.defaultRate = defaultRate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
