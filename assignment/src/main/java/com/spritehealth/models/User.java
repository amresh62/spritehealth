package com.spritehealth.models;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class User {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String email;
    private String password;
    private String phone;
    private String gender;
    private String address;

    public User() {
    }

    public User(String name, LocalDate dateOfBirth, String email, String password, 
                String phone, String gender, String address) {
        this.name = name;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.gender = gender;
        this.address = address;
    }

    // Convert Datastore Entity to User
    public static User fromEntity(Entity entity) {
        User user = new User();
        user.setId(entity.getKey().getId());
        user.setName(entity.getString("name"));
        
        String dobString = entity.getString("dateOfBirth");
        if (dobString != null && !dobString.isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(dobString, DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        user.setEmail(entity.getString("email"));
        user.setPassword(entity.getString("password"));
        user.setPhone(entity.getString("phone"));
        user.setGender(entity.getString("gender"));
        user.setAddress(entity.getString("address"));
        
        return user;
    }

    // Convert User to Datastore Entity Builder
    public Entity.Builder toEntityBuilder(Key key) {
        Entity.Builder builder = Entity.newBuilder(key)
            .set("name", this.name != null ? this.name : "")
            .set("email", this.email != null ? this.email : "")
            .set("password", this.password != null ? this.password : "")
            .set("phone", this.phone != null ? this.phone : "")
            .set("gender", this.gender != null ? this.gender : "")
            .set("address", this.address != null ? this.address : "");
        
        if (this.dateOfBirth != null) {
            builder.set("dateOfBirth", this.dateOfBirth.format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else {
            builder.set("dateOfBirth", "");
        }
        
        return builder;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
