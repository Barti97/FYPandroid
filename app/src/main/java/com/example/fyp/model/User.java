package com.example.fyp.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDate;

public class User {

//    private int userId;
    private String email;
    private String password;
    private String name;
    private String surname;
    private int phoneNumber;
    private LocalDate DoB;

    public User() {}

    public User(String email, String name, String surname, int phoneNumber, LocalDate dob) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.DoB = dob;
    }
    public User(String email, String password, String name, String surname, int phoneNumber, LocalDate dob) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.DoB = dob;
    }

    public String toString() {
        String output = "User: ";
        output += "\n\tName: " + this.name + "\n\tSurname: " + this.surname + "\n\tEmail: " + this.email + "\n\tPhone: "
                + this.phoneNumber + "\n\tDOB: " + this.DoB.toString();
        return output;
    }

    public String toJSON() {
        String string = new Gson().toJson(this);
        Log.d("string", string);
        JsonObject json = JsonParser.parseString(string).getAsJsonObject();
        json.remove("DoB");
        Log.d("removed DOB", json.toString());
        json.addProperty("DoB", this.DoB.toString());
        Log.d("added DOB", json.toString());
        return json.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getDoB() {
        return DoB;
    }

    public void setDoB(LocalDate doB) {
        DoB = doB;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

