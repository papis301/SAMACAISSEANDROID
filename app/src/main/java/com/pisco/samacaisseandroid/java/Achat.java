package com.pisco.samacaisseandroid.java;

public class Achat {
    private int id;
    private String supplierName;
    private String productName;
    private int quantity;
    private double price;
    private String date;

    public Achat(int id, String supplierName, String productName, int quantity, double price, String date) {
        this.setId(id);
        this.setSupplierName(supplierName);
        this.setProductName(productName);
        this.setQuantity(quantity);
        this.setPrice(price);
        this.setDate(date);
    }


    public double getTotal() { return price * quantity; }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
