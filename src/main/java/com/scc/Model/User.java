package com.scc.Model;

public record User(int id, String username, String password, String role, String status) {
    @Override
    public String toString(){
        return String.format("[%d] %s (%s) - status: %s", id, username, role, status);
    }
}