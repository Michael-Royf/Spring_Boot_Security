package com.michael.spring_boot_security.repository;

import com.michael.spring_boot_security.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findUserEntityByEmail(String email);

    Optional<UserEntity> findUserEntityByUserId(String userId);
}
