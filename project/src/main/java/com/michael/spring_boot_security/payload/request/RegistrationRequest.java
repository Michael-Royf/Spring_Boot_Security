package com.michael.spring_boot_security.payload.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
//@PasswordMatches
public class RegistrationRequest {
    @NotEmpty(message = "First name cannot be empty or null")
    private String firstName;
    @NotEmpty(message = "Last name cannot be empty or null")
    private String lastName;
    @NotEmpty(message = "Email cannot be empty or null")
    @Email(message = "Invalid email address")
    private String email;

   // @ValidPassword
    @NotEmpty(message = "Password cannot be empty or null")
    private String password;

//    @NotEmpty(message = "Password cannot be empty or null")
//    private String confirmationPassword;

    private String bio;

    private String phone;

}