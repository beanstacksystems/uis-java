package com.bss.uis.api.gateway.service;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bss.uis.api.gateway.model.FacebookAuthModel;
import com.bss.uis.api.gateway.model.FacebookUserModel;
import com.bss.uis.api.gateway.model.LoginMethodEnum;
import com.bss.uis.api.gateway.model.Properties;
import com.bss.uis.api.gateway.model.User;
import com.bss.uis.api.gateway.model.UserPrincipal;
import com.bss.uis.api.gateway.payload.LoginResponse;
import com.bss.uis.api.gateway.repository.UserRepository;
import com.bss.uis.api.gateway.security.JwtTokenProvider;

import net.bytebuddy.utility.RandomString;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebClient webClient;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public ResponseEntity<?> facebook(FacebookAuthModel facebookAuthModel) {
        String templateUrl = String.format(Properties.FACEBOOK_AUTH_URL, facebookAuthModel.getAuthToken());
        FacebookUserModel facebookUserModel = webClient.get().uri(templateUrl).retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    throw new ResponseStatusException(clientResponse.statusCode(), "facebook login error");
                })
                .bodyToMono(FacebookUserModel.class)
                .block();

        final Optional<User> userOptional = userRepository.findByEmail(facebookUserModel.getEmail());

        if (userOptional.isEmpty()) {        //we have no user with given email so register them
            final User user = new User(facebookUserModel.getEmail(), new RandomString(10).nextString(), LoginMethodEnum.FACEBOOK, "ROLE_USER", facebookUserModel.getEmail());
            userRepository.save(user);
            final UserPrincipal userPrincipal = new UserPrincipal(user);
            String jwt = tokenProvider.generateToken(userPrincipal);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path("/api/v1/user/{username}")
                    .buildAndExpand(facebookUserModel.getFirstName()).toUri();

            return ResponseEntity.created(location).body(new LoginResponse(Properties.TOKEN_PREFIX + jwt));
        } else { // user exists just login
            final User user = userOptional.get();
            if ((user.getLoginMethodEnum() != LoginMethodEnum.FACEBOOK)) { //check if logged in with different logged in method
                return ResponseEntity.badRequest().body("previously logged in with different login method");
            }

            UserPrincipal userPrincipal = new UserPrincipal(user);
            String jwt = tokenProvider.generateTokenWithPrinciple(userPrincipal);
            return ResponseEntity.ok(new LoginResponse(Properties.TOKEN_PREFIX + jwt));
        }
    }
}
