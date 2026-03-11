package com.locator360.core.domain.circle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircleMembershipServiceTest {

    private CircleMembershipService circleMembershipService;

    @BeforeEach
    void setUp() {
        circleMembershipService = new CircleMembershipService();
    }

    @Nested
    @DisplayName("validateMemberLimit(currentMemberCount, memberLimit)")
    class ValidateMemberLimitWithCustomLimitTests {

        @Test
        @DisplayName("should pass when below limit")
        void shouldPassWhenBelowLimit() {
            assertDoesNotThrow(() -> circleMembershipService.validateMemberLimit(3, 5));
        }

        @Test
        @DisplayName("should throw when at limit")
        void shouldThrowWhenAtLimit() {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> circleMembershipService.validateMemberLimit(5, 5));

            assertTrue(ex.getMessage().contains("5"));
        }

        @Test
        @DisplayName("should throw when above limit")
        void shouldThrowWhenAboveLimit() {
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> circleMembershipService.validateMemberLimit(6, 5));

            assertTrue(ex.getMessage().contains("5"));
        }

        @Test
        @DisplayName("should validate with premium limit of 10")
        void shouldValidateWithPremiumLimit() {
            assertDoesNotThrow(() -> circleMembershipService.validateMemberLimit(9, 10));

            assertThrows(IllegalStateException.class,
                    () -> circleMembershipService.validateMemberLimit(10, 10));
        }
    }

    @Nested
    @DisplayName("validateMemberLimit(currentMemberCount) - default limit")
    class ValidateMemberLimitWithDefaultTests {

        @Test
        @DisplayName("should use default limit of 5")
        void shouldUseDefaultLimit() {
            assertDoesNotThrow(() -> circleMembershipService.validateMemberLimit(4));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> circleMembershipService.validateMemberLimit(5));

            assertTrue(ex.getMessage().contains("5"));
        }

        @Test
        @DisplayName("should pass when zero members")
        void shouldPassWhenZeroMembers() {
            assertDoesNotThrow(() -> circleMembershipService.validateMemberLimit(0));
        }
    }
}
