package com.pisco.samacaisseandroid.java;

// Product.java
public class Product {
    private int id;
    private String name;
    private double price;
    private double quantity;
    private String unit;
    private String image;

    // Constructeur complet
    public Product(int id, String name, double price, double quantity, String unit, String image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.unit = unit;
        this.image = image;
    }

    // Optionnel : constructeur simple pour nom + prix
    public Product(int id, String name, double price) {
        this(id, name, price, 0, "", null);
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}


// CartItem.java


