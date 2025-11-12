package com.example.userAdministrationApplication.repositories;

import com.example.userAdministrationApplication.modules.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserAdministrationRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByEmail(String email);

    List<UserEntity> findByCreatedAtAfter(LocalDateTime dateTime);
    
    Page<UserEntity> findByCreatedAtAfter(LocalDateTime dateTime, Pageable pageable);
}
