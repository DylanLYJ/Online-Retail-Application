package com.example.project.items;

public class User {
    private final String username;
    private Boolean isAdmin;
    
    public User(String username, Boolean isAdmin) {
        this.username = username;
        this.isAdmin = isAdmin;
    }
    
    public String getUsername() {return username;}
    public Boolean getIsAdmin() {return isAdmin;}
    public void setIsAdmin(Boolean isAdmin) {this.isAdmin = isAdmin;}
}
