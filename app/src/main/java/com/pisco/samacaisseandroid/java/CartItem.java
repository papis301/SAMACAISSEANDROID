package com.pisco.samacaisseandroid.java;

public class CartItem {
    private Product product;
    private double  quantity;

    public CartItem(Product product, double  quantity) {
        this.setProduct(product);
        this.setQuantity(quantity);
    }

    public double getTotal() {
        return getProduct().getPrice() * getQuantity();
    }

    // Getter pratique pour éviter d'écrire item.product.name partout
    public String getName() {
        return getProduct().getName();
    }

    public double getPrice() {
        return getProduct().getPrice();
    }

    public double  getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantity(double  quantity) {
        this.quantity = quantity;
    }
}

