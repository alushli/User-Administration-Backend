package com.example.userAdministrationApplication.controllers;

import com.example.userAdministrationApplication.exceptions.DbConnectionException;
import com.example.userAdministrationApplication.exceptions.InvalidPasswordException;
import com.example.userAdministrationApplication.exceptions.UserAlreadyExistException;
import com.example.userAdministrationApplication.modules.dtos.UserDto;
import com.example.userAdministrationApplication.modules.dtos.requests.CreateNewUserRequest;
import com.example.userAdministrationApplication.modules.dtos.responses.GetUsersResponse;
import com.example.userAdministrationApplication.modules.dtos.responses.UserResponse;
import com.example.userAdministrationApplication.services.UserAdministrationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserAdministrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ControllersAdvice.class)
public class UserAdministrationControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserAdministrationService userAdministrationService;

    private UserDto dummyUserDto() {
        return new UserDto(
                1L,
                "Li",
                "Alush",
                "li.alush@example.com",
                "hashedPass",
                true,
                LocalDateTime.now()
        );
    }

    private final String REQUEST = """
                {
                    "firstName": "Li",
                    "lastName": "Alush",
                    "email": "li.alush@example.com",
                    "password": "aA1@"
                }
                """;

    @Test
    void createNewUserTest() throws Exception {
        UserDto dto = dummyUserDto();
        when(userAdministrationService.createNewUser(any(CreateNewUserRequest.class)))
                .thenReturn(new UserResponse(dto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(REQUEST))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user.id", is(dto.getId().intValue())))
                .andExpect(jsonPath("$.user.firstName", is(dto.getFirstName())))
                .andExpect(jsonPath("$.user.lastName", is(dto.getLastName())))
                .andExpect(jsonPath("$.user.email", is(dto.getEmail())));
    }

    @Test
    void createNewUserValidationErrorsTest() throws Exception {
        String invalidRequest = """
                {
                    "firstName": "",
                    "lastName": "",
                    "email": "li.alush.com",
                    "password": "@"
                }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", notNullValue()))
                .andExpect(jsonPath("$.errors.firstName", not(emptyOrNullString())))
                .andExpect(jsonPath("$.errors.lastName", not(emptyOrNullString())))
                .andExpect(jsonPath("$.errors.email", not(emptyOrNullString())))
                .andExpect(jsonPath("$.errors.password", not(emptyOrNullString())));
    }

    @Test
    void createAnExistUserTest() throws Exception {
        when(userAdministrationService.createNewUser(any(CreateNewUserRequest.class)))
                .thenThrow(new UserAlreadyExistException("li.alush@example.com"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User with email \"li.alush@example.com\" already exist"));
    }

    @Test
    void createNewUserWithInvalidLengthPasswordTest() throws Exception {
        when(userAdministrationService.createNewUser(any(CreateNewUserRequest.class)))
                .thenThrow(new InvalidPasswordException("example.com"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password for users with email suffix \"example.com\" must have at least 12 characters"));
    }


    @Test
    void createUserDbErrorTest() throws Exception {
        when(userAdministrationService.createNewUser(any(CreateNewUserRequest.class)))
                .thenThrow(new DbConnectionException("DB unreachable"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("DB unreachable"));
    }

    @Test
    void getAllUsersTest() throws Exception {
        UserDto dto1 = dummyUserDto();
        UserDto dto2 = new UserDto(2L, "Jane", "Dow", "jane.dow@example.com", "pass", true, LocalDateTime.now());
        when(userAdministrationService.getAllUsers(0, 10))
                .thenReturn(new GetUsersResponse(List.of(dto1, dto2), 2L, 1));

        mockMvc.perform(get("/users?page=0&limit=10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user", hasSize(2)))
                .andExpect(jsonPath("$.user[0].email", is(dto1.getEmail())))
                .andExpect(jsonPath("$.user[1].email", is(dto2.getEmail())))
                .andExpect(jsonPath("$.totalCount", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getAllUsersDbErrorTest() throws Exception {
        when(userAdministrationService.getAllUsers(0, 10))
                .thenThrow(new DbConnectionException("DB unreachable"));

        mockMvc.perform(get("/users?page=0&limit=10"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("DB unreachable"));
    }

    @Test
    void deactivateUserTest() throws Exception {
        mockMvc.perform(put("/users/deactivate/{id}", 5L))
                .andExpect(status().isNoContent());
        verify(userAdministrationService).deactivateUser(eq(5L));
    }

    @Test
    void deactivateUserDbErrorTest() throws Exception {
        doThrow(new DbConnectionException("DB unreachable")).when(userAdministrationService).deactivateUser(eq(5L));

        mockMvc.perform(put("/users/deactivate/{id}", 5L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("DB unreachable"));
    }

  @Test
    void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/users/{id}", 5L))
                .andExpect(status().isNoContent());
        verify(userAdministrationService).deleteUser(eq(5L));
    }

    @Test
    void deleteUserDbErrorTest() throws Exception {
        doThrow(new DbConnectionException("DB unreachable")).when(userAdministrationService).deleteUser(eq(5L));

        mockMvc.perform(delete("/users/{id}", 5L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("DB unreachable"));
    }

    @Test
    void getCreatedUsersLastDayTest() throws Exception {
        UserDto dto = dummyUserDto();
        when(userAdministrationService.getCreatedUsersLastDay(0, 10))
                .thenReturn(new GetUsersResponse(List.of(dto), 1L, 1));

        mockMvc.perform(get("/users/createdLastDay?page=0&limit=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user", hasSize(1)))
                .andExpect(jsonPath("$.user[0].email", is(dto.getEmail())))
                .andExpect(jsonPath("$.totalCount", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    void getCreatedUsersLastDay_dbError() throws Exception {
        when(userAdministrationService.getCreatedUsersLastDay(0, 10))
                .thenThrow(new DbConnectionException("DB unreachable"));

        mockMvc.perform(get("/users/createdLastDay?page=0&limit=10"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("DB unreachable"));
    }
}
