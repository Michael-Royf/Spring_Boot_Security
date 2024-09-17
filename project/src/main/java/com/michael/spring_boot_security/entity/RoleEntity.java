package com.michael.spring_boot_security.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.michael.spring_boot_security.entity.base.Auditable;
import com.michael.spring_boot_security.enumerations.Authority;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "roles")
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RoleEntity extends Auditable {
    private String name;
    private Authority authorities;
}
