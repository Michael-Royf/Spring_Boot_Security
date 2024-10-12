package com.michael.spring_boot_security.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User {
    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String bio;
    private String profileImageURL;
    private String qrCodeImageUri;
    private String lastLogin;
    private String role;
    private String authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private boolean mfa;
    private String createdAt;
    private String updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
