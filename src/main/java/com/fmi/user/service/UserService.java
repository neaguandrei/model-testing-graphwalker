package com.fmi.user.service;

import com.fmi.user.dao.entity.UserEntity;
import com.fmi.user.dao.repository.UserRepository;
import com.fmi.user.exception.BadRequestException;
import com.fmi.user.exception.NotFoundException;
import com.fmi.user.mapper.UserMapper;
import com.fmi.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    public void save(User user) throws BadRequestException {
        validateCreateUniqueFields(user);

        final UserEntity userEntity = userMapper.mapToEntity(user);
        userEntity.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(userEntity);
    }

    public void update(String email, User user) throws BadRequestException, NotFoundException {
        Optional<UserEntity> optionalUserEntity = userRepository.findByEmail(email);
        if (optionalUserEntity.isPresent()) {
            final UserEntity existingUser = optionalUserEntity.get();
            validateUpdateFields(user, existingUser);

            final UserEntity updatedUser = userMapper.mapToEntity(user);

            userMapper.mapToUpdatedEntity(updatedUser, existingUser, user.getNewPassword());
        } else {
            throw new NotFoundException("User with that email doesn't exist.");
        }
    }

    public User getByEmail(String email) throws NotFoundException {
        return userRepository.findByEmail(email).map(userMapper::mapFromEntity)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }

    public void delete(String email) throws NotFoundException {
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        if (!userEntity.isPresent()) {
            throw new NotFoundException(USER_NOT_FOUND);
        }

        final UserEntity user = userEntity.get();
        userRepository.deleteById(user.getId());
    }

    private void validateCreateUniqueFields(User user) throws BadRequestException {
        boolean isEmailExisting = userRepository.findByEmail(user.getEmail()).isPresent();
        if (isEmailExisting) {
            throw new BadRequestException("E-mail already exists!");
        }

        boolean isPhoneExisting = userRepository.findByPhone(user.getPhone()).isPresent();
        if (isPhoneExisting) {
            throw new BadRequestException("Phone already exists!");
        }
    }

    private void validateUpdateFields(User user, UserEntity existingEntity) throws BadRequestException {
        if (!user.getEmail().equals(existingEntity.getEmail())) {
            boolean isEmailExisting = userRepository.findByEmail(user.getEmail()).isPresent();
            if (isEmailExisting) {
                throw new BadRequestException("E-mail already exists!");
            }
        }

        if (!user.getPhone().equals(existingEntity.getPhone())) {
            boolean isPhoneExisting = userRepository.findByPhone(user.getPhone()).isPresent();
            if (isPhoneExisting) {
                throw new BadRequestException("Phone already exists!");
            }
        }
    }
}
