package org.example.table;

import jakarta.persistence.*;

@Entity
@Table(name = "people")
public class People {

    @Id
    @Column(name = "TgID", nullable = false)
    private long tgId;

    @Column(name = "SubscriptionTime", nullable = false, length = 255)
    private String subscriptionTime;

    @Column(name = "Marketing", nullable = false)
    private boolean marketing;

    @Column(name = "Active", nullable = false)
    private boolean Active;

    @Column(name = "Admin", nullable = false)
    private boolean admin;

    @Column(name = "Username", nullable = false, length = 255)
    private String username;

    @Column(name = "FirstName", nullable = false, length = 255)
    private String firstName;

    @Column(name = "User_flag", nullable = false)
    private boolean user_flag;

    // Геттеры и сеттеры
    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        Active = active;
    }

    public boolean isUser_flag() {
        return user_flag;
    }

    public void setUser_flag(boolean user_flag) {
        this.user_flag = user_flag;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public long getTgId() {
        return tgId;
    }

    public void setTgId(long tgId) {
        this.tgId = tgId;
    }

    public String getSubscriptionTime() {
        return subscriptionTime;
    }

    public void setSubscriptionTime(String subscriptionTime) {
        this.subscriptionTime = subscriptionTime;
    }

    public boolean isMarketing() {
        return marketing;
    }

    public void setMarketing(boolean marketing) {
        this.marketing = marketing;
    }
}
