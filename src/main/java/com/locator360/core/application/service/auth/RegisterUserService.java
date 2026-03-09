package com.locator360.core.application.service.auth;

import com.locator360.core.domain.user.*;
import com.locator360.core.port.in.auth.RegisterUserUseCase;
import com.locator360.core.port.in.dto.input.RegisterWithEmailInputDto;
import com.locator360.core.port.in.dto.input.RegisterWithPhoneInputDto;
import com.locator360.core.port.in.dto.output.RegisterUserOutputDto;
import com.locator360.core.port.out.AuthIdentityRepository;
import com.locator360.core.port.out.PasswordEncoder;
import com.locator360.core.port.out.UserRepository;
import com.locator360.core.port.out.VerificationTokenRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RegisterUserService implements RegisterUserUseCase {

  private final UserRepository userRepository;
  private final AuthIdentityRepository authIdentityRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final ModelMapper modelMapper;
  private final MeterRegistry meterRegistry;

  @Override
  public RegisterUserOutputDto registerWithEmail(RegisterWithEmailInputDto input) {
    log.debug("Registering user with email: {}", input.getEmail());

    if (userRepository.existsByEmail(input.getEmail())) {
      throw new IllegalArgumentException("Email already registered");
    }

    User user = User.create(input.getEmail(), null, input.getFullName());
    User savedUser = userRepository.save(user);

    String passwordHash = passwordEncoder.encode(input.getPassword());
    AuthIdentity authIdentity = AuthIdentity.createPassword(
        savedUser.getId(), input.getEmail(), passwordHash);
    authIdentityRepository.save(authIdentity);

    VerificationToken token = VerificationToken.create(
        savedUser.getId(), TokenType.EMAIL_VERIFICATION);
    verificationTokenRepository.save(token);

    log.info("User registered successfully with email: {}", savedUser.getId());
    meterRegistry.counter("users.registered", "method", "email").increment();
    return modelMapper.map(savedUser, RegisterUserOutputDto.class);
  }

  @Override
  public RegisterUserOutputDto registerWithPhone(RegisterWithPhoneInputDto input) {
    log.debug("Registering user with phone: {}", input.getPhoneNumber());

    if (userRepository.existsByPhoneNumber(input.getPhoneNumber())) {
      throw new IllegalArgumentException("Phone number already registered");
    }

    User user = User.create(null, input.getPhoneNumber(), input.getFullName());
    User savedUser = userRepository.save(user);

    AuthIdentity authIdentity = AuthIdentity.createPhoneSms(
        savedUser.getId(), input.getPhoneNumber());
    authIdentityRepository.save(authIdentity);

    VerificationToken token = VerificationToken.create(
        savedUser.getId(), TokenType.PHONE_VERIFICATION);
    verificationTokenRepository.save(token);

    log.info("User registered successfully with phone: {}", savedUser.getId());
    meterRegistry.counter("users.registered", "method", "phone").increment();
    return modelMapper.map(savedUser, RegisterUserOutputDto.class);
  }
}
