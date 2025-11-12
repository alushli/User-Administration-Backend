package com.example.userAdministrationApplication.modules.dtos.responses;

import com.example.userAdministrationApplication.modules.dtos.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GetUsersResponse {
    private List<UserDto> user;
    private long totalCount;
    private int totalPages;
}
