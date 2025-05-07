package com.testtask.bankcardmanagement.service.security.jwt;

import com.testtask.bankcardmanagement.exception.card.CardDuplicateException;
import com.testtask.bankcardmanagement.exception.user.UserNotFoundException;
import com.testtask.bankcardmanagement.model.User;
import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationRequest;
import com.testtask.bankcardmanagement.model.dto.auth.AuthenticationResponse;
import com.testtask.bankcardmanagement.model.dto.auth.RegistrationRequest;
import com.testtask.bankcardmanagement.model.dto.card.CardRequest;
import com.testtask.bankcardmanagement.model.dto.card.CardResponse;
import com.testtask.bankcardmanagement.model.mapper.AuthenticationMapper;
import com.testtask.bankcardmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * This is a service for authorization and registration of users
 * @see JwtService
 * @see User
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationMapper authenticationMapper;
    private final AuthenticationManager authenticationManager;

    /**
     * The method creates a new user
     * @param authenticationRequest a request object that contains the data of the registered user
     * @return an object {@link AuthenticationResponse} contains jwt token
     * @see RegistrationRequest
     * @see AuthenticationResponse
     * @see AuthenticationMapper
     */
    public AuthenticationResponse register(RegistrationRequest authenticationRequest) {
        User user = new User();
        user.setEmail(authenticationRequest.email());
        user.setPassword(passwordEncoder.encode(authenticationRequest.password()));
        user.setRole(authenticationRequest.role());
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        return authenticationMapper.toAuthenticationResponse(jwtToken);
    }

    /**
     * Method for user authentication
     * @param authenticationRequest request object that contains the authentication data
     * @return an object {@link AuthenticationResponse} contains jwt token
     * @see AuthenticationRequest
     * @see AuthenticationResponse
     * @see AuthenticationMapper
     * @throws UserNotFoundException If the user with the specified email does not found
     */
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
