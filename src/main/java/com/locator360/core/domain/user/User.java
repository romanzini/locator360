package com.locator360.core.domain.user;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class User {

    private final UUID id;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String gender;
    private String profilePhotoUrl;
    private String preferredLanguage;
    private String timezone;
    private DistanceUnit distanceUnit;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    private User(UUID id, String email, String phoneNumber, String fullName,
                 String firstName, String lastName, LocalDate birthDate, String gender,
                 String profilePhotoUrl, String preferredLanguage, String timezone,
                 DistanceUnit distanceUnit, UserStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.profilePhotoUrl = profilePhotoUrl;
        this.preferredLanguage = preferredLanguage;
        this.timezone = timezone;
        this.distanceUnit = distanceUnit;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ─── Factory: criação ───────────────────────────────────────────

    public static User create(String email, String phoneNumber, String fullName) {
        validateCreation(email, phoneNumber, fullName);

        String[] nameParts = splitName(fullName.trim());
        Instant now = Instant.now();

        return new User(
                UUID.randomUUID(),
                email,
                phoneNumber,
                fullName.trim(),
                nameParts[0],
                nameParts[1],
                null,
                null,
                null,
                "pt-BR",
                "America/Sao_Paulo",
                DistanceUnit.KM,
                UserStatus.PENDING_VERIFICATION,
                now,
                now
        );
    }

    // ─── Factory: reconstituição ────────────────────────────────────

    public static User restore(UUID id, String email, String phoneNumber,
                               String fullName, String firstName, String lastName,
                               LocalDate birthDate, String gender, String profilePhotoUrl,
                               String preferredLanguage, String timezone, DistanceUnit distanceUnit,
                               UserStatus status, Instant createdAt, Instant updatedAt) {
        return new User(id, email, phoneNumber, fullName, firstName, lastName,
                birthDate, gender, profilePhotoUrl, preferredLanguage, timezone,
                distanceUnit, status, createdAt, updatedAt);
    }

    // ─── Business methods ───────────────────────────────────────────

    public void activate() {
        if (this.status == UserStatus.BLOCKED) {
            throw new IllegalStateException("Cannot activate a blocked user");
        }
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void block() {
        if (this.status != UserStatus.ACTIVE) {
            throw new IllegalStateException("Only active users can be blocked");
        }
        this.status = UserStatus.BLOCKED;
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String fullName, String firstName, String lastName,
                              LocalDate birthDate, String gender, String profilePhotoUrl,
                              String preferredLanguage, String timezone, DistanceUnit distanceUnit) {
        if (fullName != null) {
            if (fullName.isBlank()) {
                throw new IllegalArgumentException("Full name cannot be blank");
            }
            this.fullName = fullName.trim();
            String[] nameParts = splitName(this.fullName);
            this.firstName = nameParts[0];
            this.lastName = nameParts[1];
        }
        if (firstName != null) {
            this.firstName = firstName;
        }
        if (lastName != null) {
            this.lastName = lastName;
        }
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
        if (gender != null) {
            this.gender = gender;
        }
        if (profilePhotoUrl != null) {
            this.profilePhotoUrl = profilePhotoUrl;
        }
        if (preferredLanguage != null) {
            this.preferredLanguage = preferredLanguage;
        }
        if (timezone != null) {
            this.timezone = timezone;
        }
        if (distanceUnit != null) {
            this.distanceUnit = distanceUnit;
        }
        this.updatedAt = Instant.now();
    }

    // ─── Validations ────────────────────────────────────────────────

    private static void validateCreation(String email, String phoneNumber, String fullName) {
        if ((email == null || email.isBlank()) && (phoneNumber == null || phoneNumber.isBlank())) {
            throw new IllegalArgumentException("Either email or phone number is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name is required");
        }
    }

    private static String[] splitName(String fullName) {
        int firstSpace = fullName.indexOf(' ');
        if (firstSpace < 0) {
            return new String[]{fullName, null};
        }
        return new String[]{
                fullName.substring(0, firstSpace),
                fullName.substring(firstSpace + 1)
        };
    }

    // ─── Getters ────────────────────────────────────────────────────

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getTimezone() {
        return timezone;
    }

    public DistanceUnit getDistanceUnit() {
        return distanceUnit;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
