package com.example.userAdministrationApplication.controllers;

import com.example.userAdministrationApplication.modules.dtos.requests.CreateNewUserRequest;
import com.example.userAdministrationApplication.modules.dtos.responses.GetUsersResponse;
import com.example.userAdministrationApplication.modules.dtos.responses.UserResponse;
import com.example.userAdministrationApplication.services.UserAdministrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserAdministrationController {
    private final UserAdministrationService userAdministrationService;

    @Autowired
    public UserAdministrationController(UserAdministrationService userAdministrationService) {
        this.userAdministrationService = userAdministrationService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateNewUserRequest user) {
        UserResponse response = userAdministrationService.createNewUser(user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<GetUsersResponse> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(0) int limit) {
        GetUsersResponse users = userAdministrationService.getAllUsers(page, limit);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable long id) {
        userAdministrationService.deactivateUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        userAdministrationService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/createdLastDay")
    public ResponseEntity<GetUsersResponse> getCreatedUsersLastDay(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(0) int limit) {
        GetUsersResponse users = userAdministrationService.getCreatedUsersLastDay(page, limit);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }
}
