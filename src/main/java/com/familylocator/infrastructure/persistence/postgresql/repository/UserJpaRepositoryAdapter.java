package com.familylocator.infrastructure.persistence.postgresql.repository;

import com.familylocator.core.domain.user.User;
import com.familylocator.core.port.out.UserRepository;
import com.familylocator.infrastructure.persistence.postgresql.entity.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserJpaRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;
    private final ModelMapper modelMapper;

    @Override
    public User save(User user) {
        log.debug("Saving user: {}", user.getId());
        UserJpaEntity entity = modelMapper.map(user, UserJpaEntity.class);
        UserJpaEntity savedEntity = springDataUserRepository.save(entity);
        return modelMapper.map(savedEntity, User.class);
    }

    @Override
    public Optional<User> findById(UUID id) {
        log.debug("Finding user by id: {}", id);
        return springDataUserRepository.findById(id)
                .map(entity -> modelMapper.map(entity, User.class));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return springDataUserRepository.findByEmail(email)
                .map(entity -> modelMapper.map(entity, User.class));
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        log.debug("Finding user by phone: {}", phoneNumber);
        return springDataUserRepository.findByPhoneNumber(phoneNumber)
                .map(entity -> modelMapper.map(entity, User.class));
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return springDataUserRepository.existsByPhoneNumber(phoneNumber);
    }
}
