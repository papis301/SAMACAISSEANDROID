package com.pisco.samacaisseandroid.java;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
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

    public int getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

