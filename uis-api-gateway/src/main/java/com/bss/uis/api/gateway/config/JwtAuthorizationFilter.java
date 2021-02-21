package com.bss.uis.api.gateway.config;

import com.bss.uis.api.gateway.model.Properties;
import com.bss.uis.api.gateway.model.User;
import com.bss.uis.api.gateway.model.UserPrincipal;
import com.bss.uis.api.gateway.repository.UserRepository;
import com.bss.uis.api.gateway.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(Properties.HEADER_STRING);
        if (header == null || !header.startsWith(Properties.TOKEN_PREFIX)) {
//        	System.out.println("#############################################");
//        	System.out.println("Authorisation Header: "+(header==null?header:header.startsWith(Properties.TOKEN_PREFIX)));
//        	System.out.println("#############################################");
            chain.doFilter(request, response);
            return;
        }
        Authentication authentication = getUsernamePasswordAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private Authentication getUsernamePasswordAuthentication(HttpServletRequest request) {
        String token = request.getHeader(Properties.HEADER_STRING)
                .replace(Properties.TOKEN_PREFIX, "");

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            String email = tokenProvider.getUserNameFromJWT(token);

            if (email != null) {
                Optional<User> user = userRepository.findByEmail( email);
                if (user.isPresent()) {
                    UserPrincipal principal = new UserPrincipal(user.get());
                    return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                }
            }
            return null;
        }
        return null;
    }

}
