package com.locator360.infrastructure.persistence.postgresql.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity {

  @Id
  private UUID id;

  @Column(unique = true)
  private String email;

  @Column(name = "phone_number", unique = true)
  private String phoneNumber;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  private String gender;

  @Column(name = "profile_photo_url")
  private String profilePhotoUrl;

  @Column(name = "preferred_language", nullable = false)
  private String preferredLanguage;

  @Column(nullable = false)
  private String timezone;

  @Column(name = "distance_unit", nullable = false)
  private String distanceUnit;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
