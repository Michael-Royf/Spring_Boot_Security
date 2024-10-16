package com.michael.spring_boot_security.utility;

import com.michael.spring_boot_security.domain.User;
import com.michael.spring_boot_security.entity.CredentialEntity;
import com.michael.spring_boot_security.entity.RoleEntity;
import com.michael.spring_boot_security.entity.UserEntity;
import com.michael.spring_boot_security.exception.payload.ApiException;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.michael.spring_boot_security.constans.AppConstants.MICHAEL_APP;
import static com.michael.spring_boot_security.constans.AppConstants.NINETY_DAYS;
import static dev.samstevens.totp.util.Utils.getDataUriForImage;

public class UserUtils {

    public static User fromUserEntity(UserEntity userEntity,
                                      RoleEntity role,
                                      CredentialEntity credentialEntity) {
        User user = new User();
        BeanUtils.copyProperties(userEntity, user);
        user.setLastLogin(userEntity.getLastLogin().toString());
        user.setCredentialsNonExpired(isCredentialNonExpired(credentialEntity));
        user.setCreatedAt(userEntity.getCreatedAt().toString());
        user.setUpdatedAt(userEntity.getUpdatedAt().toString());
        user.setRole(role.getName());
        user.setAuthorities(role.getAuthorities().getValue());
        return user;
    }


    private static boolean isCredentialNonExpired(CredentialEntity credentialEntity) {
        return credentialEntity.getUpdatedAt().plusDays(NINETY_DAYS).isAfter(LocalDateTime.now());
    }
    public static BiFunction<String, String, QrData> qrDataFunction = (email, qrCodeSecret) ->
            new QrData.Builder()
                    .issuer(MICHAEL_APP)
                    .label(email)
                    .secret(qrCodeSecret)
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

    public static BiFunction<String, String, String> qrCodeImageUri = (email, qrCodeSecret) -> {
        var data = qrDataFunction.apply(email, qrCodeSecret);
        var generator = new ZxingPngQrGenerator();
        byte[] imageData;
        try {
            imageData = generator.generate(data);
        } catch (Exception exception) {
         //   throw new ApiException(exception.getMessage());
            throw new ApiException("Unable to create QR code URI");
        }
        return getDataUriForImage(imageData, generator.getImageMimeType());
    };

    public static Supplier<String> qrCodeSecret = () -> new DefaultSecretGenerator().generate();

}
