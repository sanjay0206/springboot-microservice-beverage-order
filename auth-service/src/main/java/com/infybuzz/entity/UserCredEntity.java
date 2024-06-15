package com.infybuzz.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class UserCredEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime createdAt;

    public UserCredEntity(String username, String email, String password, Role role, LocalDateTime createdAt) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }
}
