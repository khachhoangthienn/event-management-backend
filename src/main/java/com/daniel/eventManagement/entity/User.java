package com.daniel.eventManagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "\"user\"")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String userId;
    @Column(unique = true)
    @Email
    String email;
    String password;
    String firstName;
    String lastName;
    String birth;
    String bio;
    boolean gender;
    String address;
    String avatarUrl;
    String role;
}
