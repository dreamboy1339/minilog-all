package com.asdf.minilog.controller;

import com.asdf.minilog.dto.AuthenticationRequestDto;
import com.asdf.minilog.dto.AuthenticationResponseDto;
import com.asdf.minilog.dto.UserResponseDto;
import com.asdf.minilog.service.UserService;
import com.asdf.minilog.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/auth")
public class AuthenticationController {
  private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

  private AuthenticationManager authenticationManager;
  private JwtUtil jwtTokenUtil;
  private UserDetailsService userDetailsService;
  private UserService userService;

  @Autowired
  public AuthenticationController(
      AuthenticationManager authenticationManager,
      JwtUtil jwtTokenUtil,
      UserDetailsService userDetailsService,
      UserService userService) {
    this.authenticationManager = authenticationManager;
    this.jwtTokenUtil = jwtTokenUtil;
    this.userDetailsService = userDetailsService;
    this.userService = userService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> createAuthenticationToken(
      @RequestBody AuthenticationRequestDto authRequest) {
    try {
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken(
              authRequest.getUsername(), authRequest.getPassword());
      authenticationManager.authenticate(authenticationToken);
      UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
      UserResponseDto userResponseDto = userService.getUserByUsername(userDetails.getUsername());
      String jwtToken = jwtTokenUtil.generateToken(userDetails, userResponseDto.getId());
      AuthenticationResponseDto authenticationResponseDto =
          AuthenticationResponseDto.builder().jwt(jwtToken).build();
      return ResponseEntity.ok(authenticationResponseDto);
    } catch (BadCredentialsException e) {
      logger.error("Authentication failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("An error occurred during authentication");
    }
  }
}
