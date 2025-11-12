package com.example.userAdministrationApplication.services;

import com.example.userAdministrationApplication.exceptions.DbConnectionException;
import com.example.userAdministrationApplication.exceptions.UserAlreadyExistException;
import com.example.userAdministrationApplication.modules.dtos.UserDto;
import com.example.userAdministrationApplication.modules.dtos.requests.CreateNewUserRequest;
import com.example.userAdministrationApplication.modules.dtos.responses.GetUsersResponse;
import com.example.userAdministrationApplication.modules.dtos.responses.UserResponse;
import com.example.userAdministrationApplication.modules.entities.UserEntity;
import com.example.userAdministrationApplication.repositories.UserAdministrationRepository;
import com.example.userAdministrationApplication.utils.ConvertorUtil;
import com.example.userAdministrationApplication.utils.ValidationUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UserAdministrationServiceImpl implements UserAdministrationService {
    private final UserAdministrationRepository repository;
    private final PasswordEncoder passwordEncoder;
    final static Logger logger = LoggerFactory.getLogger(UserAdministrationServiceImpl.class);

    @Override
    @Transactional
    @Retryable(retryFor = { DataAccessException.class },
            maxAttempts = 3,
            noRetryFor = { UserAlreadyExistException.class },
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000))
    public UserResponse createNewUser(CreateNewUserRequest user) {
        logger.info("Creating new user with email {}", user.getEmail());
        ValidationUtil.validatePasswordForSpecificUsers(user.getEmail(), user.getPassword());
        UserEntity userEntity = new UserEntity(user, passwordEncoder);

        try {
            repository.save(userEntity);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistException(user.getEmail());
        }
        return new UserResponse(ConvertorUtil.convertToUserDto(userEntity));
    }

    @Override
    @Transactional
    @Retryable(retryFor = { DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000))
    public GetUsersResponse getAllUsers(int page, int limit) {
        logger.info("Getting all users with page {} and limit {}", page, limit);
        Pageable pageable = PageRequest.of(page, limit);
        Page<UserEntity> userPage = repository.findAll(pageable);

        return new GetUsersResponse(
                getUsersDto(userPage),
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );
    }

    @Override
    @Transactional
    @Retryable(retryFor = { DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000))
    public void deactivateUser(long id) {
        logger.info("Deactivate user with id {}", id);
        repository.findById(id).ifPresent(user -> {
            user.setActive(false);
            repository.save(user);
        });
    }

    @Override
    @Transactional
    @Retryable(retryFor = { DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000))
    public void deleteUser(long id) {
        logger.info("Deleting user with id {}", id);
        repository.deleteById(id);
    }

    @Override
    @Transactional
    @Retryable(retryFor = { DataAccessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000))
    public GetUsersResponse getCreatedUsersLastDay(int page, int limit) {
        logger.info("Getting users created in last day with page {} and limit {}", page, limit);
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        Pageable pageable = PageRequest.of(page, limit);
        Page<UserEntity> userPage = repository.findByCreatedAtAfter(twentyFourHoursAgo, pageable);

        return new GetUsersResponse(
                getUsersDto(userPage),
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );
    }

    private List<UserDto> getUsersDto(Page<UserEntity> userPage) {
        return userPage.getContent()
                .stream()
                .map(ConvertorUtil::convertToUserDto)
                .toList();
    }

    @Recover
    public UserResponse recoverSaveUser(DataAccessException e, CreateNewUserRequest request){
        logger.error("Failed to save user with email {}", request.getEmail());
        throw new DbConnectionException(String.format("Failed to save user with email %s from data base", request.getEmail()));
    }

    @Recover
    public void recoverDeleteUser(DataAccessException e, long id){
        logger.error("Failed to delete user with id {}", id);
        throw new DbConnectionException(String.format("Failed to delete user with id %s from data base", id));
    }

    @Recover
    public GetUsersResponse recoverGetUsersGeneric(DataAccessException e) {
      logger.error("Failed to get users");
      throw new DbConnectionException("Failed to get users from data base");
    }
}
