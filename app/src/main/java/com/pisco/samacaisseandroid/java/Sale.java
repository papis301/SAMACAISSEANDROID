package com.pisco.samacaisseandroid.java;

public class Sale {
    private int id;
    private String date;
    private double total;
    private String clientName;

    public Sale(int id, String date, double total, String clientName) {
        this.id = id;
        this.date = date;
        this.total = total;
        this.clientName = clientName;
    }

    public int getId() { return id; }
    public String getDate() { return date; }
    public double getTotal() { return total; }
    public String getClientName() { return clientName; }
}

