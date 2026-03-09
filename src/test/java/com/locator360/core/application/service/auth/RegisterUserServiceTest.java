package com.locator360.core.application.service.auth;

import com.locator360.core.domain.user.*;
import com.locator360.core.port.in.dto.input.RegisterWithEmailInputDto;
import com.locator360.core.port.in.dto.input.RegisterWithPhoneInputDto;
import com.locator360.core.port.in.dto.output.RegisterUserOutputDto;
import com.locator360.core.port.out.AuthIdentityRepository;
import com.locator360.core.port.out.PasswordEncoder;
import com.locator360.core.port.out.UserRepository;
import com.locator360.core.port.out.VerificationTokenRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthIdentityRepository authIdentityRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    @InjectMocks
    private RegisterUserService registerUserService;

    // ─── RegisterWithEmail ──────────────────────────────────────────

    @Nested
    @DisplayName("registerWithEmail")
    class RegisterWithEmailTests {

        private RegisterWithEmailInputDto validInput;

        @BeforeEach
        void setUp() {
            validInput = new RegisterWithEmailInputDto(
                    "maria@example.com",
                    "SenhaForte123!",
                    "Maria Oliveira");
            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        }

        @Test
        @DisplayName("should register user successfully with email and password")
        void shouldRegisterUserSuccessfully() {
            when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
            when(passwordEncoder.encode("SenhaForte123!")).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            RegisterUserOutputDto expectedOutput = RegisterUserOutputDto.builder()
                    .id(UUID.randomUUID())
                    .email("maria@example.com")
                    .fullName("Maria Oliveira")
                    .status("PENDING_VERIFICATION")
                    .build();
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(expectedOutput);

            RegisterUserOutputDto result = registerUserService.registerWithEmail(validInput);

            assertNotNull(result);
            assertEquals("maria@example.com", result.getEmail());
            assertEquals("Maria Oliveira", result.getFullName());
            assertEquals("PENDING_VERIFICATION", result.getStatus());
        }

        @Test
        @DisplayName("should save User via repository")
        void shouldSaveUser() {
            when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(RegisterUserOutputDto.builder().build());

            registerUserService.registerWithEmail(validInput);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertEquals("maria@example.com", savedUser.getEmail());
            assertEquals("Maria Oliveira", savedUser.getFullName());
            assertEquals("Maria", savedUser.getFirstName());
            assertEquals("Oliveira", savedUser.getLastName());
            assertEquals(UserStatus.PENDING_VERIFICATION, savedUser.getStatus());
        }

        @Test
        @DisplayName("should create AuthIdentity with PASSWORD provider")
        void shouldCreateAuthIdentityWithPasswordProvider() {
            when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
            when(passwordEncoder.encode("SenhaForte123!")).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(RegisterUserOutputDto.builder().build());

            registerUserService.registerWithEmail(validInput);

            ArgumentCaptor<AuthIdentity> captor = ArgumentCaptor.forClass(AuthIdentity.class);
            verify(authIdentityRepository).save(captor.capture());

            AuthIdentity savedIdentity = captor.getValue();
            assertEquals(AuthProvider.PASSWORD, savedIdentity.getProvider());
            assertEquals("maria@example.com", savedIdentity.getEmail());
            assertEquals("hashed_password", savedIdentity.getPasswordHash());
            assertFalse(savedIdentity.isVerified());
        }

        @Test
        @DisplayName("should create EMAIL_VERIFICATION token")
        void shouldCreateEmailVerificationToken() {
            when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(RegisterUserOutputDto.builder().build());

            registerUserService.registerWithEmail(validInput);

            ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(verificationTokenRepository).save(captor.capture());

            VerificationToken savedToken = captor.getValue();
            assertEquals(TokenType.EMAIL_VERIFICATION, savedToken.getType());
            assertNotNull(savedToken.getToken());
            assertNotNull(savedToken.getExpiresAt());
        }

        @Test
        @DisplayName("should encode password before saving")
        void shouldEncodePassword() {
            when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
            when(passwordEncoder.encode("SenhaForte123!")).thenReturn("bcrypt_hash");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(RegisterUserOutputDto.builder().build());

            registerUserService.registerWithEmail(validInput);

            verify(passwordEncoder).encode("SenhaForte123!");

            ArgumentCaptor<AuthIdentity> captor = ArgumentCaptor.forClass(AuthIdentity.class);
            verify(authIdentityRepository).save(captor.capture());
            assertEquals("bcrypt_hash", captor.getValue().getPasswordHash());
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail("maria@example.com")).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> registerUserService.registerWithEmail(validInput));

            assertEquals("Email already registered", exception.getMessage());
            verify(userRepository, never()).save(any());
            verify(authIdentityRepository, never()).save(any());
            verify(verificationTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("should map User to RegisterUserOutputDto using ModelMapper")
        void shouldMapUserToOutputDto() {
            when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            RegisterUserOutputDto expectedOutput = RegisterUserOutputDto.builder()
                    .id(UUID.randomUUID())
                    .email("maria@example.com")
                    .fullName("Maria Oliveira")
                    .build();
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(expectedOutput);

            RegisterUserOutputDto result = registerUserService.registerWithEmail(validInput);

            verify(modelMapper).map(any(User.class), eq(RegisterUserOutputDto.class));
            assertSame(expectedOutput, result);
        }
    }

    // ─── RegisterWithPhone ──────────────────────────────────────────

    @Nested
    @DisplayName("registerWithPhone")
    class RegisterWithPhoneTests {

        private RegisterWithPhoneInputDto validInput;

        @BeforeEach
        void setUp() {
            validInput = new RegisterWithPhoneInputDto(
                    "+5511999999999",
                    "123456",
                    "João da Silva");
            lenient().when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
        }

        @Test
        @DisplayName("should register user successfully with phone number")
        void shouldRegisterUserSuccessfully() {
            when(userRepository.existsByPhoneNumber("+5511999999999")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            RegisterUserOutputDto expectedOutput = RegisterUserOutputDto.builder()
                    .id(UUID.randomUUID())
                    .phoneNumber("+5511999999999")
                    .fullName("João da Silva")
                    .status("PENDING_VERIFICATION")
                    .build();
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(expectedOutput);

            RegisterUserOutputDto result = registerUserService.registerWithPhone(validInput);

            assertNotNull(result);
            assertEquals("+5511999999999", result.getPhoneNumber());
            assertEquals("João da Silva", result.getFullName());
        }

        @Test
        @DisplayName("should create User with phone and no email")
        void shouldCreateUserWithPhoneAndNoEmail() {
            when(userRepository.existsByPhoneNumber("+5511999999999")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(RegisterUserOutputDto.builder().build());

            registerUserService.registerWithPhone(validInput);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertNull(savedUser.getEmail());
            assertEquals("+5511999999999", savedUser.getPhoneNumber());
            assertEquals("João da Silva", savedUser.getFullName());
        }

        @Test
        @DisplayName("should create AuthIdentity with PHONE_SMS provider")
        void shouldCreateAuthIdentityWithPhoneSmsProvider() {
            when(userRepository.existsByPhoneNumber("+5511999999999")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(RegisterUserOutputDto.builder().build());

            registerUserService.registerWithPhone(validInput);

            ArgumentCaptor<AuthIdentity> captor = ArgumentCaptor.forClass(AuthIdentity.class);
            verify(authIdentityRepository).save(captor.capture());

            AuthIdentity savedIdentity = captor.getValue();
            assertEquals(AuthProvider.PHONE_SMS, savedIdentity.getProvider());
            assertEquals("+5511999999999", savedIdentity.getPhoneNumber());
            assertTrue(savedIdentity.isVerified());
        }

        @Test
        @DisplayName("should create PHONE_VERIFICATION token")
        void shouldCreatePhoneVerificationToken() {
            when(userRepository.existsByPhoneNumber("+5511999999999")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(authIdentityRepository.save(any(AuthIdentity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(verificationTokenRepository.save(any(VerificationToken.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(any(User.class), eq(RegisterUserOutputDto.class)))
                    .thenReturn(RegisterUserOutputDto.builder().build());

            registerUserService.registerWithPhone(validInput);

            ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(verificationTokenRepository).save(captor.capture());

            VerificationToken savedToken = captor.getValue();
            assertEquals(TokenType.PHONE_VERIFICATION, savedToken.getType());
            assertNotNull(savedToken.getToken());
        }

        @Test
        @DisplayName("should throw exception when phone number already exists")
        void shouldThrowWhenPhoneAlreadyExists() {
            when(userRepository.existsByPhoneNumber("+5511999999999")).thenReturn(true);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> registerUserService.registerWithPhone(validInput));

            assertEquals("Phone number already registered", exception.getMessage());
            verify(userRepository, never()).save(any());
            verify(authIdentityRepository, never()).save(any());
            verify(verificationTokenRepository, never()).save(any());
        }
    }
}
