package com.school.erp;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGenTest {

    @Test
    public void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("HASH_START");
        System.out.println(encoder.encode("schooly123"));
        System.out.println("HASH_END");
    }
}
