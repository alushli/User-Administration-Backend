package com.example.userAdministrationApplication.modules.dtos.responses;

import com.example.userAdministrationApplication.modules.dtos.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserResponse {
    private UserDto user;
}
