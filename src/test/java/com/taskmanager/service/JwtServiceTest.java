package com.taskmanager.service;

import com.taskmanager.entity.Role;
import com.taskmanager.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void generatesTokenWithSubjectRoleAndValidates() {

        JwtService jwtService = new JwtService(SECRET, 24);

        User user = User.builder()
                .username("elena_mikhaylova")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        assertEquals("elena_mikhaylova", jwtService.extractUsername(token));
        assertEquals(Role.USER, jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token, "elena_mikhaylova"));
    }
}
