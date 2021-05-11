package com.fmi.user.mapper;

import com.fmi.user.dao.entity.UserEntity;
import com.fmi.user.dto.UserDto;
import com.fmi.user.exception.BadRequestException;
import com.fmi.user.model.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    public UserDto mapToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    public User mapFromDto(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }

    public UserEntity mapToEntity(User user) {
        return modelMapper.map(user, UserEntity.class);
    }

    public User mapFromEntity(UserEntity entity) {
        return modelMapper.map(entity, User.class);
    }


    public void mapToUpdatedEntity(UserEntity updatedEntity, UserEntity existingEntity, String newPassword) throws BadRequestException {
        if (!passwordEncoder.matches(updatedEntity.getPassword(), existingEntity.getPassword())) {
            throw new BadRequestException("Please enter a correct password before proceeding with the update of your profile.");
        }

        if (updatedEntity.getPassword() != null && newPassword != null) {
            existingEntity.setPassword(passwordEncoder.encode(newPassword));
        }
        existingEntity.setEmail(updatedEntity.getEmail());
        existingEntity.setFirstName(updatedEntity.getFirstName());
        existingEntity.setLastName(updatedEntity.getLastName());
        existingEntity.setPhone(updatedEntity.getPhone());
    }
}
