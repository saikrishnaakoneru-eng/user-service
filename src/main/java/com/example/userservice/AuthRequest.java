package com.example.userservice;

import lombok.Data;

@Data
public class AuthRequest {
    private String name;
    private String password;
}