package com.michael.spring_boot_security.service.impl;

import com.michael.spring_boot_security.cache.CacheStore;
import com.michael.spring_boot_security.domain.User;
import com.michael.spring_boot_security.entity.ConfirmationEntity;
import com.michael.spring_boot_security.entity.CredentialEntity;
import com.michael.spring_boot_security.entity.RoleEntity;
import com.michael.spring_boot_security.entity.UserEntity;
import com.michael.spring_boot_security.entity.base.RequestContext;
import com.michael.spring_boot_security.enumerations.Authority;
import com.michael.spring_boot_security.enumerations.EventType;
import com.michael.spring_boot_security.enumerations.LoginType;
import com.michael.spring_boot_security.event.UserEvent;
import com.michael.spring_boot_security.exception.payload.NotFoundException;
import com.michael.spring_boot_security.payload.request.RegistrationRequest;
import com.michael.spring_boot_security.repository.ConfirmationRepository;
import com.michael.spring_boot_security.repository.CredentialRepository;
import com.michael.spring_boot_security.repository.RoleRepository;
import com.michael.spring_boot_security.repository.UserRepository;
import com.michael.spring_boot_security.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.michael.spring_boot_security.utility.UserUtils.fromUserEntity;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@Transactional(rollbackOn = Exception.class)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    public static final String NO_ROLE_FOUND_BY_NAME = "No role found by name: %s";
    public static final String CONFIRMATION_KEY_NOT_FOUND = "Confirmation key not found";
    public static final String NO_USER_FOUND_BY_EMAIL = "No user found by email: %s ";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;
    private final ApplicationEventPublisher publisher;
    private final CacheStore<String, Integer> userCache;


    @Override
    public void createUser(RegistrationRequest request) throws IOException {

        var userEntity = createNewUser(request);

        userRepository.save(userEntity);
        var credentialEntity = new CredentialEntity(request.getPassword(), userEntity);//TODO: encode password
        credentialRepository.save(credentialEntity);
        var confirmationEntity = new ConfirmationEntity(userEntity);
        confirmationRepository.save(confirmationEntity);
        publisher.publishEvent(new UserEvent(userEntity, EventType.REGISTRATION, Map.of("key", confirmationEntity.getKey())));

    }

    @Override
    public RoleEntity getRoleName(String name) {
        return roleRepository.findByName(name).orElseThrow(() ->
                new NotFoundException(String.format(NO_ROLE_FOUND_BY_NAME, name)));
    }

    @Override
    public void verifyAccountKey(String key) {
        ConfirmationEntity confirmationEntity = getUserConfirmation(key);
        UserEntity userEntity = findUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
        userEntity.setEnabled(true);
        userRepository.save(userEntity);
        confirmationRepository.delete(confirmationEntity);
    }

    @Override
    public void updateLoginAttempt(String email, LoginType loginType) {
        var userEntity = findUserEntityByEmail(email);
        RequestContext.setUserId(userEntity.getId());
        switch (loginType) {
            case LOGIN_ATTEMPT -> {
                if (userCache.get(userEntity.getEmail()) == null) {
                    userEntity.setLoginAttempts(0);
                    userEntity.setAccountNonLocked(true);
                }
                userEntity.setLoginAttempts(userEntity.getLoginAttempts() + 1);
                userCache.put(userEntity.getEmail(), userEntity.getLoginAttempts());
                if (userCache.get(userEntity.getEmail()) > 5) {
                    userEntity.setAccountNonLocked(false);
                }
            }
            case LOGIN_SUCCESS -> {
                userEntity.setAccountNonLocked(true);
                userEntity.setLoginAttempts(0);
                userEntity.setLastLogin(LocalDateTime.now());
                userCache.evict(userEntity.getEmail());
            }
        }
        userRepository.save(userEntity);
    }

    @Override
    public User getUserByUserId(String userId) {
        var userEntity = findUserEntityById(userId);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public User getUserByEmail(String email) {
        UserEntity user = findUserEntityByEmail(email);
        return fromUserEntity(user, user.getRole(), getUserCredentialById(user.getId()));
    }


    @Override
    public CredentialEntity getUserCredentialById(Long userId) {
        return credentialRepository.getCredentialEntityByUserEntityId(userId)
                .orElseThrow(() -> new NotFoundException("Unable to find credential"));


    }

    private UserEntity findUserEntityById(String userId) {
        return userRepository.findUserEntityByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserEntity findUserEntityByEmail(String email) {
        return userRepository.findUserEntityByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(NO_USER_FOUND_BY_EMAIL, email)));
    }

    private ConfirmationEntity getUserConfirmation(String key) {
        return confirmationRepository.findByKey(key)
                .orElseThrow(() -> new NotFoundException(CONFIRMATION_KEY_NOT_FOUND));
    }


    private UserEntity createNewUser(RegistrationRequest request) {
        var role = getRoleName(Authority.USER.name());
        return createUserEntity(request.getFirstName(), request.getLastName(), request.getEmail(), role);
    }

    private UserEntity createUserEntity(String firstName, String lastName, String email, RoleEntity role) {
        return UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .firstName(firstLetterUpper(firstName))
                .lastName(firstLetterUpper(lastName))
                .email(email)
                .role(role)
                .lastLogin(LocalDateTime.now())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .mfa(false)
                .enabled(false)
                .loginAttempts(0)
                .qrCodeSecret(EMPTY)
                .phone(EMPTY)//TODO: fix
                .bio(EMPTY)
                .imageUrl("https://cdn-icons-png.flaticon.com/512/149/149071.png")
                .build();
    }


    private String firstLetterUpper(String world) {
        return Arrays.stream(world.split(" "))
                .filter(word -> !word.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
