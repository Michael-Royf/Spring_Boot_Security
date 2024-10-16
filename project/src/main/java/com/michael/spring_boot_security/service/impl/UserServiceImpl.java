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
import com.michael.spring_boot_security.exception.payload.ApiException;
import com.michael.spring_boot_security.exception.payload.NotFoundException;
import com.michael.spring_boot_security.payload.request.RegistrationRequest;
import com.michael.spring_boot_security.payload.request.ResetPasswordRequest;
import com.michael.spring_boot_security.payload.request.RoleRequest;
import com.michael.spring_boot_security.payload.request.UpdatePasswordRequest;
import com.michael.spring_boot_security.repository.ConfirmationRepository;
import com.michael.spring_boot_security.repository.CredentialRepository;
import com.michael.spring_boot_security.repository.RoleRepository;
import com.michael.spring_boot_security.repository.UserRepository;
import com.michael.spring_boot_security.service.UserService;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.michael.spring_boot_security.constans.AppConstants.EXISTING_PASSWORD_INCORRECT;
import static com.michael.spring_boot_security.utility.UserUtils.*;
import static com.michael.spring_boot_security.validations.UserValidation.verifyAccountStatus;
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
    private final PasswordEncoder passwordEncoder;


    @Override
    public void createUser(RegistrationRequest request) throws IOException {

        var userEntity = createNewUser(request);

        userRepository.save(userEntity);
        var credentialEntity = new CredentialEntity(passwordEncoder.encode(request.getPassword()), userEntity);//TODO: encode password
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


    //https://github.com/samdjstevens/java-totp?ysclid=m28rwbumql236354684
    @Override
    public User setUpMfa(Long id) {
        var userEntity = findUserEntityById(id);
        var codeSecret = qrCodeSecret.get();
        userEntity.setQrCodeImageUri(qrCodeImageUri.apply(userEntity.getEmail(), codeSecret));
        userEntity.setQrCodeSecret(codeSecret);
        userEntity.setMfa(true);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public User cancelMfa(Long id) {
        var userEntity = findUserEntityById(id);
        userEntity.setMfa(false);
        userEntity.setQrCodeSecret(EMPTY);
        userEntity.setQrCodeImageUri(EMPTY);
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public User verifyQrCode(String userId, String qrCode) {
        var userEntity = findUserEntityById(userId);
        verifyCode(qrCode, userEntity.getQrCodeSecret());
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void resetPassword(String email) {
        var user = findUserEntityByEmail(email);
        var confirmation = getUserConfirmation(user);
        if (confirmation != null) {
            //send existing confirmation

        } else {
            var confirmationEntity = new ConfirmationEntity(user);
            confirmationRepository.save(confirmationEntity);
            publisher.publishEvent(new UserEvent(user, EventType.RESET_PASSWORD, Map.of("key", confirmationEntity.getKey())));
        }
    }

    @Override
    public User verifyPassword(String key) {
        var confirmationEntity = getUserConfirmation(key);
        if (confirmationEntity == null) {
            throw new ApiException("Unable to find token");
        }
        var userEntity = findUserEntityByEmail(confirmationEntity.getUserEntity().getEmail());
        if (userEntity == null) {
            throw new ApiException("Incorrect token");
        }
        verifyAccountStatus(userEntity);
        confirmationRepository.delete(confirmationEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updatePassword(ResetPasswordRequest resetPasswordRequest) {
        if (!resetPasswordRequest.getNewPassword().equals(resetPasswordRequest.getConfirmationPassword())) {
            throw new ApiException("Password don't match. Please try again");
        }
        var user = getUserByUserId(resetPasswordRequest.getUserId());
        var credentials = getUserCredentialById(user.getId());
        credentials.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        credentialRepository.save(credentials);
    }

    @Override
    public void updatePassword(String userId, UpdatePasswordRequest updatePasswordRequest) {
        if (!updatePasswordRequest.getNewPassword().equals(updatePasswordRequest.getConfirmationPassword())) {
            throw new ApiException("Password don't match. Please try again");
        }
        var userEntity = findUserEntityById(userId);
        var credentialsEntity = getUserCredentialById(userEntity.getId());
        verifyAccountStatus(userEntity);
        if (!passwordEncoder.matches(updatePasswordRequest.getCurrentPassword(), credentialsEntity.getPassword())) {
            throw new ApiException(EXISTING_PASSWORD_INCORRECT);
        }
        credentialsEntity.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
        credentialRepository.save(credentialsEntity);
    }


    @Override
    public User updateUser(String userId, RegistrationRequest registrationRequest) {
        //TODO: check email;
        var userEntity = findUserEntityById(userId);
        userEntity.setFirstName(registrationRequest.getFirstName());
        userEntity.setLastName(registrationRequest.getLastName());
        userEntity.setEmail(registrationRequest.getEmail());
        userEntity.setBio(registrationRequest.getBio());
        userEntity.setPhone(registrationRequest.getPhone());
        userRepository.save(userEntity);
        return fromUserEntity(userEntity, userEntity.getRole(), getUserCredentialById(userEntity.getId()));
    }

    @Override
    public void updateRole(String userId, RoleRequest roleRequest) {
        var userEntity = findUserEntityById(userId);
        userEntity.setRole(getRoleName(roleRequest.getRole()));
        userRepository.save(userEntity);
    }

    //

    @Override
    public void toggleAccountExpired(String userId) {
        var userEntity = findUserEntityById(userId);
        userEntity.setAccountNonExpired(!userEntity.isAccountNonExpired());
        userRepository.save(userEntity);
    }

    @Override
    public void toggleAccountLocked(String userId) {
        var userEntity = findUserEntityById(userId);
        userEntity.setAccountNonLocked(!userEntity.isAccountNonLocked());
        userRepository.save(userEntity);
    }

    @Override
    public void toggleAccountEnabled(String userId) {
        var userEntity = findUserEntityById(userId);
        userEntity.setEnabled(!userEntity.isEnabled());
        userRepository.save(userEntity);
    }

    //TODO:????
    @Override
    public void toggleCredentialsExpired(String userId) {
        var userEntity = findUserEntityById(userId);
        var credentials = getUserCredentialById(userEntity.getId());
        credentials.setUpdatedAt(LocalDateTime.of(1995, 7, 12, 11, 11));
        /*if (credentials.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(now())) {
            credentials.setUpdatedAt(now());
        } else {
            credentials.setUpdatedAt(LocalDateTime.of(1995, 7, 12, 11, 11));
        }*/
        credentialRepository.save(credentials);
    }

    private boolean verifyCode(String qrCode, String qrCodeSecret) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        if (codeVerifier.isValidCode(qrCodeSecret, qrCode)) {
            return true;
        } else {
            throw new ApiException("Invalid QR code. Please try again");
        }
    }

    private UserEntity findUserEntityById(String userId) {
        return userRepository.findUserEntityByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserEntity findUserEntityById(Long id) {
        return userRepository.findById(id)
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

    private ConfirmationEntity getUserConfirmation(UserEntity user) {
        return confirmationRepository.findByUserEntity(user)
                .orElse(null);
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
