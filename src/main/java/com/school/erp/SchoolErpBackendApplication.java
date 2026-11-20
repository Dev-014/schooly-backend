package com.school.erp;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SchoolErpBackendApplication {

    static {
        loadDotenv();
    }

    public static void loadDotenv() {
        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(SchoolErpBackendApplication.class, args);
    }

}
