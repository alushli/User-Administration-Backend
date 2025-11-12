package com.example.userAdministrationApplication.services;

import com.example.userAdministrationApplication.modules.dtos.requests.CreateNewUserRequest;
import com.example.userAdministrationApplication.modules.dtos.responses.GetUsersResponse;
import com.example.userAdministrationApplication.modules.dtos.responses.UserResponse;
import com.example.userAdministrationApplication.modules.entities.UserEntity;
import com.example.userAdministrationApplication.repositories.UserAdministrationRepository;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserAdministrationServiceTests {
    @Autowired
    private UserAdministrationService userAdministrationService;

    @Autowired
    private UserAdministrationRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private CreateNewUserRequest createUserRequest(String email) {
        return new CreateNewUserRequest("Li", "Alush", email, "SecurePass123!");
    }

    @Test
    void createNewUserTest() {
        CreateNewUserRequest request = createUserRequest("li.alush@example.com");

        UserResponse response = userAdministrationService.createNewUser(request);

        assertNotNull(response);
        assertNotNull(response.getUser());
        assertNotNull(response.getUser().getId());
        assertEquals("Li", response.getUser().getFirstName());
        assertEquals("Alush", response.getUser().getLastName());
        assertEquals("li.alush@example.com", response.getUser().getEmail());
        assertTrue(response.getUser().getActive());
        assertNotNull(response.getUser().getCreatedAt());

        Optional<UserEntity> savedUser = repository.findById(response.getUser().getId());
        assertTrue(savedUser.isPresent());
        assertTrue(passwordEncoder.matches("SecurePass123!", savedUser.get().getPassword()));
    }

    @Test
    void createNewUserDuplicateEmailTest() {
        CreateNewUserRequest request1 = createUserRequest("li.alush@example.com");
        CreateNewUserRequest request2 = createUserRequest("li.alush@example.com");

        userAdministrationService.createNewUser(request1);

        assertThrows(Exception.class, () -> {
            userAdministrationService.createNewUser(request2);
        });
    }

    @Test
    void createNewUserInvalidPasswordForSpecificDomainTest() {
        CreateNewUserRequest request = new CreateNewUserRequest(
                "Li",
                "Alush",
                "li.alush@example.com",
                "Short1!"
        );

        assertThrows(Exception.class, () -> {
            userAdministrationService.createNewUser(request);
        });
    }

    @Test
    void createNewUserValidPasswordForSpecificDomainTest() {
        CreateNewUserRequest request = new CreateNewUserRequest(
                "Li",
                "Alush",
                "li.alush@example.com",
                "VeryLongPassword123!"
        );

        UserResponse response = userAdministrationService.createNewUser(request);

        assertNotNull(response);
        assertEquals("li.alush@example.com", response.getUser().getEmail());
    }

    @Test
    void createNewUserPasswordIsHashedTest() {
        CreateNewUserRequest request = createUserRequest("li.alush@example.com");
        String plainPassword = request.getPassword();

        UserResponse response = userAdministrationService.createNewUser(request);

        Optional<UserEntity> savedUser = repository.findById(response.getUser().getId());
        assertTrue(savedUser.isPresent());
        assertNotEquals(plainPassword, savedUser.get().getPassword());
        assertTrue(passwordEncoder.matches(plainPassword, savedUser.get().getPassword()));
    }

    @Test
    void getAllUsersNoUsersTest() {
        GetUsersResponse response = userAdministrationService.getAllUsers(0, 10);

        assertNotNull(response);
        assertNotNull(response.getUser());
        assertTrue(response.getUser().isEmpty());
    }

    @Test
    void getAllUsersMultipleUsersTest() {
        userAdministrationService.createNewUser(createUserRequest("user1@test.com"));
        userAdministrationService.createNewUser(createUserRequest("user2@test.com"));
        userAdministrationService.createNewUser(createUserRequest("user3@test.com"));

        GetUsersResponse response = userAdministrationService.getAllUsers(0, 10);

        assertNotNull(response);
        assertEquals(3, response.getUser().size());
        assertThat(response.getUser())
                .extracting("email")
                .containsExactlyInAnyOrder("user1@test.com", "user2@test.com", "user3@test.com");
    }

    @Test
    void getAllUsersIncludesInactiveUsersTest() {
        UserResponse user1 = userAdministrationService.createNewUser(createUserRequest("active@test.com"));
        UserResponse user2 = userAdministrationService.createNewUser(createUserRequest("inactive@test.com"));

        userAdministrationService.deactivateUser(user2.getUser().getId());

        GetUsersResponse response = userAdministrationService.getAllUsers(0, 10);

        assertEquals(2, response.getUser().size());
    }

    @Test
    void deactivateUserSuccessTest() {
        UserResponse created = userAdministrationService.createNewUser(createUserRequest("user@test.com"));
        Long userId = created.getUser().getId();

        userAdministrationService.deactivateUser(userId);

        Optional<UserEntity> deletedUser = repository.findById(userId);
        assertTrue(deletedUser.isPresent());
        assertFalse(deletedUser.get().getActive());
    }

    @Test
    void deactivateUserNonExistentTest() {
        assertDoesNotThrow(() -> {
            userAdministrationService.deactivateUser(10L);
        });
    }

    @Test
    void deactivateUserAlreadyInactiveTest() {
        UserResponse created = userAdministrationService.createNewUser(createUserRequest("double.delete@test.com"));
        Long userId = created.getUser().getId();

        userAdministrationService.deactivateUser(userId);
        userAdministrationService.deactivateUser(userId);

        Optional<UserEntity> user = repository.findById(userId);
        assertTrue(user.isPresent());
        assertFalse(user.get().getActive());
    }

    @Test
    void deleteUserSuccessTest() {
        UserResponse created = userAdministrationService.createNewUser(createUserRequest("user@test.com"));
        Long userId = created.getUser().getId();

        userAdministrationService.deleteUser(userId);

        Optional<UserEntity> deletedUser = repository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void deleteUserNonExistentTest() {
        assertDoesNotThrow(() -> {
            userAdministrationService.deleteUser(10L);
        });
    }

    @Test
    void deleteUserAlreadyInactiveTest() {
        UserResponse created = userAdministrationService.createNewUser(createUserRequest("double.delete@test.com"));
        Long userId = created.getUser().getId();

        userAdministrationService.deleteUser(userId);
        userAdministrationService.deleteUser(userId);

        Optional<UserEntity> deletedUser = repository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void getCreatedUsersLastDayRecentUsersTest() {
        userAdministrationService.createNewUser(createUserRequest("recent1@test.com"));
        userAdministrationService.createNewUser(createUserRequest("recent2@test.com"));

        GetUsersResponse response = userAdministrationService.getCreatedUsersLastDay(0, 10);

        assertNotNull(response);
        assertEquals(2, response.getUser().size());
    }

    @Test
    void getCreatedUsersLastDayOldUsersTest() {
        UserEntity oldUser = new UserEntity();
        oldUser.setFirstName("Old");
        oldUser.setLastName("User");
        oldUser.setEmail("old@test.com");
        oldUser.setPassword(passwordEncoder.encode("password"));
        oldUser.setActive(true);
        oldUser.setCreatedAt(LocalDateTime.now().minusDays(2));
        repository.save(oldUser);

        GetUsersResponse response = userAdministrationService.getCreatedUsersLastDay(0, 10);

        assertNotNull(response);
        assertTrue(response.getUser().isEmpty());
    }

    @Test
    void getCreatedUsersLastDayMixedTimestampsTest() {
        userAdministrationService.createNewUser(createUserRequest("recent@test.com"));

        UserEntity oldUser = new UserEntity();
        oldUser.setFirstName("Old");
        oldUser.setLastName("User");
        oldUser.setEmail("old@test.com");
        oldUser.setPassword(passwordEncoder.encode("password"));
        oldUser.setActive(true);
        oldUser.setCreatedAt(LocalDateTime.now().minusDays(2));
        repository.save(oldUser);

        GetUsersResponse response = userAdministrationService.getCreatedUsersLastDay(0, 10);

        assertEquals(1, response.getUser().size());
        assertEquals("recent@test.com", response.getUser().get(0).getEmail());
    }

    @Test
    void getCreatedUsersLastDayNoUsersTest() {
        GetUsersResponse response = userAdministrationService.getCreatedUsersLastDay(0, 10);

        assertNotNull(response);
        assertTrue(response.getUser().isEmpty());
    }

    @Test
    void fullUserLifecycleTest() {
        CreateNewUserRequest request = createUserRequest("lifecycle@test.com");
        UserResponse created = userAdministrationService.createNewUser(request);
        Long userId = created.getUser().getId();

        GetUsersResponse allUsers1 = userAdministrationService.getAllUsers(0, 10);
        assertEquals(1, allUsers1.getUser().size());
        assertTrue(allUsers1.getUser().getFirst().getActive());

        userAdministrationService.deactivateUser(userId);

        GetUsersResponse allUsers2 = userAdministrationService.getAllUsers(0, 10);
        assertEquals(1, allUsers2.getUser().size());
        assertFalse(allUsers2.getUser().getFirst().getActive());
    }
}
