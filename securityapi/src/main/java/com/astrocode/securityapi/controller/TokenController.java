package com.astrocode.securityapi.controller;

import com.astrocode.securityapi.dtos.LoginRequest;
import com.astrocode.securityapi.dtos.LoginResponse;
import com.astrocode.securityapi.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/login")
public class TokenController {

    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public TokenController(JwtEncoder jwtEncoder, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest){
       var user = userRepository.findByUsername(loginRequest.username());

       if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, passwordEncoder)){
           throw new BadCredentialsException("User or password is invalid");
       }

        //Configure the jwt attributes (claims) to send
       var now = Instant.now();
       var expiresIn = 300L;
       var claims = JwtClaimsSet.builder()
               .issuer("mybackend")
               .subject(user.get().getUserId().toString())
               .issuedAt(now)
               .expiresAt(now.plusSeconds(expiresIn))
               .build();

       var jwtValue  = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

       return ResponseEntity.ok(new LoginResponse(jwtValue, expiresIn));

    }

}