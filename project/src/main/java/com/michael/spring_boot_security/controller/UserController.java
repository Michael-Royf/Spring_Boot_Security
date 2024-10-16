package com.michael.spring_boot_security.controller;

import com.michael.spring_boot_security.domain.User;
import com.michael.spring_boot_security.enumerations.TokenType;
import com.michael.spring_boot_security.handler.ApiLogoutHandler;
import com.michael.spring_boot_security.payload.request.*;
import com.michael.spring_boot_security.payload.response.Response;
import com.michael.spring_boot_security.service.JwtService;
import com.michael.spring_boot_security.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static com.michael.spring_boot_security.utility.RequestUtils.getResponse;
import static java.util.Collections.emptyMap;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final ApiLogoutHandler logoutHandler;

    @PostMapping("/register")
    public ResponseEntity<Response> saveUser(@RequestBody @Valid RegistrationRequest registrationRequest,
                                             HttpServletRequest request) throws IOException {
        userService.createUser(registrationRequest);
        return ResponseEntity.created(URI.create(""))
                .body(getResponse(
                        request,
                        emptyMap(),
                        "Account created. Check your email to enable your account",
                        HttpStatus.CREATED));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyAccount(@RequestParam("key") String key,
                                                  HttpServletRequest request) {
        userService.verifyAccountKey(key);
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "Account verified",
                        HttpStatus.OK));
    }

    @PatchMapping("/mfa/setup")
    public ResponseEntity<Response> setupMfa(@AuthenticationPrincipal User userPrincipal,
                                             HttpServletRequest request) throws IOException {
        var user = userService.setUpMfa(userPrincipal.getId());
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        Map.of("user", user),
                        "MFA set up successfully",
                        HttpStatus.OK));
    }

    @PatchMapping("/mfa/cancel")
    public ResponseEntity<Response> cancelMfa(@AuthenticationPrincipal User userPrincipal,
                                              HttpServletRequest request) throws IOException {
        var user = userService.cancelMfa(userPrincipal.getId());
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        Map.of("user", user),
                        "MFA canceled successfully",
                        HttpStatus.OK));
    }

    @PostMapping("/verify/qrcode")
    public ResponseEntity<Response> verifyQrCode(@RequestBody QrCodeRequest qrCodeRequest,
                                                 HttpServletResponse response,
                                                 HttpServletRequest request) throws IOException {
        var user = userService.verifyQrCode(qrCodeRequest.getUserId(), qrCodeRequest.getQrCode());
        jwtService.addCookie(response, user, TokenType.ACCESS);
        jwtService.addCookie(response, user, TokenType.REFRESH);
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        Map.of("user", user),
                        "QR code verified",
                        HttpStatus.OK));
    }

    //START- reset password when not logged in

    @PostMapping("/reset_password")
    public ResponseEntity<Response> resetPassword(@RequestBody @Valid EmailRequest emailRequest,
                                                  HttpServletRequest request) throws IOException {
        userService.resetPassword(emailRequest.getEmail());
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "We sent you an email to reset your password",
                        HttpStatus.OK));
    }

    @GetMapping("/verify/password")
    public ResponseEntity<Response> verifyPassword(@RequestParam("key") String key,
                                                   HttpServletRequest request) throws IOException {
        var user = userService.verifyPassword(key);
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        Map.of("user", user),
                        "Enter new password",
                        HttpStatus.OK));
    }

    @PostMapping("/reset_password/reset")
    public ResponseEntity<Response> doResetPassword(@RequestBody @Valid ResetPasswordRequest resetPasswordRequest,
                                                    HttpServletRequest request) throws IOException {
        userService.updatePassword(resetPasswordRequest);
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "Password reset successfully",
                        HttpStatus.OK));
    }

    //END- reset password when not logged in


    //reset password when user is logged
    @PatchMapping("/update_password")
    public ResponseEntity<Response> updatePassword(@AuthenticationPrincipal User user,
                                                   @RequestBody UpdatePasswordRequest updatePasswordRequest,
                                                   HttpServletRequest request) {
        userService.updatePassword(user.getUserId(), updatePasswordRequest);
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "Password updated successfully.",
                        HttpStatus.OK));
    }

    @GetMapping("/profile")
    public ResponseEntity<Response> profile(@AuthenticationPrincipal User userPrincipal,
                                            HttpServletRequest request) {
        var user = userService.getUserByUserId(userPrincipal.getUserId());
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        Map.of("user", user),
                        "Profile retrieved.",
                        HttpStatus.OK));
    }


    @PatchMapping("/update")
    public ResponseEntity<Response> updateUserProfile(@AuthenticationPrincipal User userPrincipal,
                                                      @RequestBody RegistrationRequest registrationRequest,
                                                      HttpServletRequest request) {
        var user = userService.updateUser(userPrincipal.getUserId(), registrationRequest);
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        Map.of("user", user),
                        "User profile updated successfully.",
                        HttpStatus.OK));
    }


    @PatchMapping("/update_role")
    public ResponseEntity<Response> updateUserRole(@AuthenticationPrincipal User userPrincipal,
                                                   @RequestBody RoleRequest roleRequest,
                                                   HttpServletRequest request) {
        userService.updateRole(userPrincipal.getUserId(), roleRequest);
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "Role update successfully.",
                        HttpStatus.OK));
    }

    //
    @PatchMapping("/toggle_account_expired")
    public ResponseEntity<Response> toggleAccountExpired(@AuthenticationPrincipal User user,
                                                         HttpServletRequest request) {
        userService.toggleAccountExpired(user.getUserId());
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "Account updated successfully.",
                        HttpStatus.OK));
    }

    @PatchMapping("/toggle_account_locked")
    public ResponseEntity<Response> toggleAccountLocked(@AuthenticationPrincipal User user,
                                                        HttpServletRequest request) {
        userService.toggleAccountLocked(user.getUserId());
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "Account updated successfully.",
                        HttpStatus.OK));
    }

    @PatchMapping("/toggle_account_enabled")
    public ResponseEntity<Response> toggleAccountEnabled(@AuthenticationPrincipal User user,
                                                         HttpServletRequest request) {
        userService.toggleAccountEnabled(user.getUserId());
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "Account updated successfully.",
                        HttpStatus.OK));
    }

    @PatchMapping("/toggle_credentials_expired")
    public ResponseEntity<Response> toggleCredentialsExpired(@AuthenticationPrincipal User user,
                                                             HttpServletRequest request) {
        userService.toggleCredentialsExpired(user.getUserId());
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "Account updated successfully.",
                        HttpStatus.OK));
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletResponse response,
                                           HttpServletRequest request,
                                           Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        logoutHandler.logout(request, response, authentication);
        return ResponseEntity.ok()
                .body(getResponse(
                        request,
                        emptyMap(),
                        "You've logged out successfully",
                        HttpStatus.OK));
    }

}