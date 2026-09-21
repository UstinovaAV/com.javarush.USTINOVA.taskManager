package com.javarush.ustinova.taskManager.service;

import com.javarush.ustinova.taskManager.entity.enums.Role;
import com.javarush.ustinova.taskManager.security.CustomUserDetails;
import com.javarush.ustinova.taskManager.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(
                1L, "testuser", "password123",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void generateToken_and_ExtractClaims_Success() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
        assertEquals("testuser", jwtService.extractUsername(token));
        assertEquals(1L, jwtService.extractUserId(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}