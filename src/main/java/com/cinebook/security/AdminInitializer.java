package com.cinebook.security;

import com.cinebook.dao.UserDAO;
import com.cinebook.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Initializes the default system Administrator account securely if not present.
 * Uses environment variable CINEBOOK_ADMIN_PASSWORD or configured properties.
 */
@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Value("${cinebook.admin.username:admin}")
    private String adminUsername;

    @Value("${cinebook.admin.password:#{null}}")
    private String adminPasswordConfig;

    public AdminInitializer(PasswordEncoder passwordEncoder) {
        this.userDAO = new UserDAO();
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            Optional<User> existing = userDAO.findByUsername(adminUsername);
            if (existing.isPresent()) {
                return;
            }

            // Retrieve admin password from Environment Variable first, then config, then fallback default
            String envPassword = System.getenv("CINEBOOK_ADMIN_PASSWORD");
            String finalPassword = envPassword != null && !envPassword.trim().isEmpty() 
                    ? envPassword.trim() 
                    : (adminPasswordConfig != null && !adminPasswordConfig.trim().isEmpty() ? adminPasswordConfig.trim() : "Admin@CineBook2026");

            String encoded = passwordEncoder.encode(finalPassword);
            User admin = new User(adminUsername, encoded, "CineBook Administrator", "ADMIN", null);
            userDAO.createUser(admin);
            System.out.println(">>> [SECURITY] Initial admin account initialized successfully: " + adminUsername);

        } catch (SQLException e) {
            System.err.println("Notice initializing admin account: " + e.getMessage());
        }
    }
}
