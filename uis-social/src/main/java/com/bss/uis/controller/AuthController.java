package com.bss.uis.controller;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bss.uis.exception.BadRequestException;
import com.bss.uis.model.AuthProvider;
import com.bss.uis.model.User;
import com.bss.uis.payload.ApiResponse;
import com.bss.uis.payload.AuthResponse;
import com.bss.uis.payload.LoginRequest;
import com.bss.uis.payload.SignUpRequest;
import com.bss.uis.repository.UserRepository;
import com.bss.uis.security.TokenProvider;
import com.bss.uis.security.oauth2.CustomOAuth2UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TokenProvider tokenProvider;

	@Autowired
	private CustomOAuth2UserService customOAuth2UserService;

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String token = tokenProvider.createToken(authentication);
		return ResponseEntity.ok(new AuthResponse(token));
	}

	@PostMapping("/validate/{registrationId}")
	public ResponseEntity<AuthResponse> validateToken(@RequestHeader String idToken, @RequestHeader String authCode,
			@PathVariable String registrationId) throws GeneralSecurityException, IOException {

		OAuth2AuthenticationToken oAuth2AuthenticationToken = null;
		ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(registrationId);

		if (registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
			oAuth2AuthenticationToken = getAuthTokenForGoogleUser(idToken, authCode, clientRegistration);
		} else if (registrationId.equalsIgnoreCase(AuthProvider.facebook.toString())) {
			oAuth2AuthenticationToken = getAuthTokenForFacebookUser(idToken, authCode, clientRegistration);
		} else if (registrationId.equalsIgnoreCase(AuthProvider.github.toString())) {
//        	return new GithubOAuth2UserInfo(attributes);
		}

		if (null == oAuth2AuthenticationToken) {
			ResponseEntity.badRequest().build();
		}
		
		Authentication authentication = authenticationManager.authenticate(oAuth2AuthenticationToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);

		return ResponseEntity.ok(new AuthResponse(tokenProvider.createToken(oAuth2AuthenticationToken)));
	}

	private OAuth2AuthenticationToken getAuthTokenForFacebookUser(String idToken, String authCode, ClientRegistration clientRegistration) {
		throw new UnsupportedOperationException("Facebook authentication is not yet supported");
	}

	@SuppressWarnings("unchecked")
	private OAuth2AuthenticationToken getAuthTokenForGoogleUser(String idToken, String authCode,
			ClientRegistration clientRegistration)
			throws IOException, GeneralSecurityException {
		
		OAuth2AuthenticationToken oAuth2AuthenticationToken = null;
		String accessToken = exchangeAuthTokenForGoogleAccessToken(authCode, clientRegistration);

		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new ApacheHttpTransport(),
				GsonFactory.getDefaultInstance())
						// Specify the CLIENT_ID of the app that accesses the backend:
						.setAudience(Collections.singletonList(clientRegistration.getClientId()))
						// Or, if multiple clients access the backend:
						// .setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
						.build();

		// (Receive idTokenString by HTTPS POST)
		GoogleIdToken googleIdToken = verifier.verify(idToken);

		if (googleIdToken != null) {
			Payload payload = googleIdToken.getPayload();
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> userAttributes = objectMapper.convertValue(payload, Map.class);
			oAuth2AuthenticationToken = getOAuth2AuthenticationToken(idToken, clientRegistration.getRegistrationId(), accessToken,
					clientRegistration, userAttributes);
		}

		return oAuth2AuthenticationToken;
	}

	private OAuth2AuthenticationToken getOAuth2AuthenticationToken(String idToken, String registrationId,
			String accessToken, ClientRegistration clientRegistration, Map<String, Object> userAttributes) {

		OAuth2AccessToken oAuth2AccessToken = new OAuth2AccessToken(TokenType.BEARER, accessToken,
				Instant.ofEpochMilli((Long) userAttributes.get("iat")),
				Instant.ofEpochMilli((Long) userAttributes.get("exp")));
		OAuth2UserAuthority oAuth2UserAuthority = new OAuth2UserAuthority(userAttributes);

		Map<String, Object> additionalParameters = new HashMap<>();
		additionalParameters.put("id_token", idToken);

		OAuth2UserRequest oAuth2UserRequest = new OAuth2UserRequest(clientRegistration, oAuth2AccessToken,
				additionalParameters);

		OAuth2User oAuth2User = customOAuth2UserService.loadUser(oAuth2UserRequest);

		Set<GrantedAuthority> authorities = new LinkedHashSet<>();
		authorities.add(oAuth2UserAuthority);
		for (String authority : oAuth2AccessToken.getScopes()) {
			authorities.add(new SimpleGrantedAuthority("SCOPE_" + authority));
		}

		return new OAuth2AuthenticationToken(oAuth2User, authorities, registrationId);
	}

	private String exchangeAuthTokenForGoogleAccessToken(String authCode, ClientRegistration clientRegistration)
			throws IOException {

		// Exchange auth code for access token
		GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(new NetHttpTransport(),
				GsonFactory.getDefaultInstance(), "https://www.googleapis.com/oauth2/v4/token",
				clientRegistration.getClientId(), clientRegistration.getClientSecret(), authCode,
				"http://localhost:8080/oauth2/callback/" + clientRegistration.getRegistrationId())
//    	              clientRegistration.getRedirectUriTemplate())  // Specify the same redirect URI that you use with your web
						// app. If you don't have a web version of your app, you can
						// specify an empty string.
						.execute();

		return tokenResponse.getAccessToken();
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {

		if (userRepository.existsByEmail(signUpRequest.getEmail()).booleanValue()) {
			throw new BadRequestException("Email address already in use.");
		}

		// Creating user's account
		User user = new User();
		user.setName(signUpRequest.getName());
		user.setEmail(signUpRequest.getEmail());
		user.setPassword(signUpRequest.getPassword());
		user.setProvider(AuthProvider.local);
		user.setPassword(passwordEncoder.encode(user.getPassword()));

		User result = userRepository.save(user);

		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/me")
				.buildAndExpand(result.getId()).toUri();

		return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully@"));
	}

}
