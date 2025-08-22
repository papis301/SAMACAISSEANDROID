package com.pisco.samacaisseandroid.java;

public class Supplier {
    private int id;
    private String name;
    private String phone;
    private String address;

    public Supplier(int id, String name, String phone, String address) {
        this.setId(id);
        this.setName(name);
        this.setPhone(phone);
        this.setAddress(address);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

