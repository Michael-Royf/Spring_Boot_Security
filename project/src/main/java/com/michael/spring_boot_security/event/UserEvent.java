package com.michael.spring_boot_security.event;

import com.michael.spring_boot_security.entity.UserEntity;
import com.michael.spring_boot_security.enumerations.EventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class UserEvent {
    private UserEntity user;
    private EventType type;
    private Map<?, ?> data;
}
