package com.testtask.bankcardmanagement.service.jwt;

import com.testtask.bankcardmanagement.exception.UserDuplicateException;
import com.testtask.bankcardmanagement.exception.UserNotFoundException;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationRequest;
import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationResponse;
import com.testtask.bankcardmanagement.model.dto.auth.RegistrationRequest;
import com.testtask.bankcardmanagement.model.enums.UserRole;
import com.testtask.bankcardmanagement.model.mapper.AuthenticationMapper;
import com.testtask.bankcardmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationMapper authenticationMapper;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegistrationRequest authenticationRequest) {
        User user = new User();
        user.setEmail(authenticationRequest.email());
        user.setPassword(passwordEncoder.encode(authenticationRequest.password()));
        user.setRole(UserRole.USER);
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        return authenticationMapper.toAuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.email(),
                        authenticationRequest.password()
                )
        );
        // if user email and password correct
        User user = userRepository.findUserByEmail(authenticationRequest.email())
                .orElseThrow(() -> new UserNotFoundException("User with such email not found."));

        String jwtToken = jwtService.generateToken(user);
        return authenticationMapper.toAuthenticationResponse(jwtToken);
    }

}
