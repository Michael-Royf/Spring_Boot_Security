package com.michael.spring_boot_security.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.michael.spring_boot_security.entity.base.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "users")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
//Когда вы добавляете эту аннотацию к классу или полю, это указывает Jackson включить в JSON только те свойства, которые имеют значения, отличные от значений по умолчанию. Это помогает уменьшить размер JSON и делает его более компактным и читабельным.
public class UserEntity extends Auditable {
    @Column(updatable = false, unique = true, nullable = false)
    private String userId;
    @Column(name = "first_name", nullable = false)
    private String firstName;
    @Column(name = "last_name", nullable = false)
    private String lastName;
    @Column(unique = true, nullable = false)
    private String email;
    private Integer loginAttempts;
    private LocalDateTime lastLogin;
    private String phone;
    private String bio;
    private String imageUrl;//profile image
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean enabled;
    private boolean mfa;
    @JsonIgnore
    private String qrCodeSecret;
    @Column(columnDefinition = "text")
    private String qrCodeImageUri;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns =  @JoinColumn(
                    name = "role_id", referencedColumnName = "id"
            )
    )
    private RoleEntity role;
}
