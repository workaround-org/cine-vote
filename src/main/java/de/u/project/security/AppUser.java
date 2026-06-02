package de.u.project.security;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;

/**
 * Admin user for form authentication. The single {@code @UserDefinition} entity
 * in the application. Passwords are stored as BCrypt MCF strings.
 */
@Entity
@Table(name = "app_user")
@UserDefinition
public class AppUser extends PanacheEntity {

    @Username
    public String username;

    @Password
    public String password;

    @Roles
    public String role;

    public static AppUser add(String username, String bcryptPassword, String role) {
        AppUser user = new AppUser();
        user.username = username;
        user.password = bcryptPassword;
        user.role = role;
        user.persist();
        return user;
    }
}
