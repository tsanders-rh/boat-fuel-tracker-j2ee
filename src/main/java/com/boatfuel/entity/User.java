package com.boatfuel.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User entity using Quarkus Hibernate ORM with Panache
 */
@Entity
@Table(name = "USERS")
public class User extends PanacheEntityBase {

    @Id
    @Column(name = "USER_ID", length = 50)
    public String userId;

    @Column(name = "EMAIL", unique = true, length = 255)
    public String email;

    @Column(name = "DISPLAY_NAME", length = 255)
    public String displayName;

    @Column(name = "PASSWORD_HASH", length = 255)
    public String passwordHash;

    @Column(name = "IS_ADMIN")
    public Boolean isAdmin;

    @Column(name = "CREATED_AT")
    public LocalDateTime createdAt;

    @Column(name = "LAST_LOGIN")
    public LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<FuelUp> fuelUps;

    // Default constructor
    public User() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Static helper methods for Panache queries
    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static User findByUserId(String userId) {
        return findById(userId);
    }

    public static List<User> findAdmins() {
        return list("isAdmin", true);
    }
}
