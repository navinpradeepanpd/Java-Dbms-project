package com.cinebook.security;

import com.cinebook.dao.UserDAO;
import com.cinebook.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Custom Spring Security UserDetailsService querying the users table via UserDAO.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDAO userDAO;

    public CustomUserDetailsService() {
        this.userDAO = new UserDAO();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Optional<User> opt = userDAO.findByUsername(username);
            if (opt.isEmpty()) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            User u = opt.get();
            String roleName = "ROLE_" + (u.getRole() != null ? u.getRole().toUpperCase() : "CUSTOMER");
            Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleName));

            return new CustomUserDetails(u, authorities);
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Database error loading user: " + username, e);
        }
    }
}
