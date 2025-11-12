package com.example.userAdministrationApplication.utils;

import com.example.userAdministrationApplication.modules.dtos.UserDto;
import com.example.userAdministrationApplication.modules.entities.UserEntity;

public class ConvertorUtil {
    public static UserDto convertToUserDto(UserEntity entity) {
        return new UserDto(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getActive(),
                entity.getCreatedAt()
        );
    }
}
