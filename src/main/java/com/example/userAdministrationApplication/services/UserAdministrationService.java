package com.example.userAdministrationApplication.services;

import com.example.userAdministrationApplication.modules.dtos.requests.CreateNewUserRequest;
import com.example.userAdministrationApplication.modules.dtos.responses.GetUsersResponse;
import com.example.userAdministrationApplication.modules.dtos.responses.UserResponse;

public interface UserAdministrationService {
    UserResponse createNewUser(CreateNewUserRequest user);

    GetUsersResponse getAllUsers(int page, int limit);

    void deactivateUser(long id);

    void deleteUser(long id);

    GetUsersResponse getCreatedUsersLastDay(int page, int limit);

}
