package com.example.userAdministrationApplication.services;

import com.example.userAdministrationApplication.exceptions.DbConnectionException;
import com.example.userAdministrationApplication.modules.dtos.requests.CreateNewUserRequest;
import com.example.userAdministrationApplication.modules.dtos.responses.UserResponse;
import com.example.userAdministrationApplication.modules.entities.UserEntity;
import com.example.userAdministrationApplication.repositories.UserAdministrationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserAdministrationServiceRetryTests {
    @Autowired
    private UserAdministrationService service;

    @MockitoBean
    private UserAdministrationRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    private CreateNewUserRequest request(String email) {
        return new CreateNewUserRequest("Li", "Alush", email, "VeryStrong123!");
    }

    @Test
    void createNewUserRetriesThenSucceedsTest() {
        when(repository.save(any(UserEntity.class)))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #1"))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #2"))
                .thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = service.createNewUser(request("li.alush@test.com"));

        assertNotNull(response);
        assertEquals("li.alush@test.com", response.getUser().getEmail());
        verify(repository, times(3)).save(any(UserEntity.class));
    }

    @Test
    void createNewUserRetriesThenRecoverThrowsDbConnectionTest() {
        when(repository.save(any(UserEntity.class)))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #1"))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #2"))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #3"));

        DbConnectionException ex = assertThrows(DbConnectionException.class,
                () -> service.createNewUser(request("li.alush@test.com")));

        assertTrue(ex.getMessage().contains("Failed to save user with email li.alush@test.com from data base"));
        verify(repository, times(3)).save(any(UserEntity.class));
    }

    @Test
    void getAllUsersRetriesThenRecoverTest() {
        when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #1"))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #2"))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #3"));

        DbConnectionException ex = assertThrows(DbConnectionException.class,
                () -> service.getAllUsers(0, 10));

        assertTrue(ex.getMessage().contains("Failed to get users from data base"));
        verify(repository, times(3)).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void deactivateUserRetriesThenRecoverTest() {
        when(repository.findById(any(Long.class)))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #1"))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #2"))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #3"));

        DbConnectionException ex = assertThrows(DbConnectionException.class,
                () -> service.deactivateUser(2L));

        assertTrue(ex.getMessage().contains("Failed to delete user with id 2 from data base"));
        verify(repository, times(3)).findById(2L);
    }

    @Test
    void deleteUserRetriesThenRecoverTest() {
        doThrow(new DataAccessResourceFailureException("DB unreachable #1"))
                .doThrow(new DataAccessResourceFailureException("DB unreachable #2"))
                .doThrow(new DataAccessResourceFailureException("DB unreachable #3"))
                .doNothing()
                .when(repository)
                .deleteById(2L);

        DbConnectionException ex = assertThrows(DbConnectionException.class,
                () -> service.deleteUser(2L));

        assertTrue(ex.getMessage().contains("Failed to delete user with id 2 from data base"));
        verify(repository, times(3)).deleteById(2L);
    }

    @Test
    void getCreatedUsersLastDayRetriesThenRecoverTest() {
        when(repository.findByCreatedAtAfter(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #1"))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #2"))
                .thenThrow(new DataAccessResourceFailureException("DB unreachable #3"));

        DbConnectionException ex = assertThrows(DbConnectionException.class,
                () -> service.getCreatedUsersLastDay(0, 10));

        assertTrue(ex.getMessage().contains("Failed to get users from data base"));
        verify(repository, times(3)).findByCreatedAtAfter(any(), any(org.springframework.data.domain.Pageable.class));
    }
}
