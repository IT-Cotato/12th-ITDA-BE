package com.cotato.itda.domain.member.entity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String rawPassword = "qwer1234"; // 테스트하고 싶은 비밀번호
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("========================================");
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("Encoded Password: " + encodedPassword);
        System.out.println("========================================");
    }
}
