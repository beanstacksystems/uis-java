package com.bss.uis.api.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import com.bss.uis.api.gateway.security.JwtTokenProvider;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() throws Exception {
        return new JwtAuthorizationFilter(authenticationManager());
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(){
        return new JwtTokenProvider();
    }

    @Bean
    public WebClient webClient(){
        return WebClient.create();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        	.csrf()
        	.disable().sessionManagement()
        	.sessionCreationPolicy(SessionCreationPolicy.STATELESS)     //no cookies
        	
        	.and().cors()
        	
            .and()
            	.authorizeRequests()
	            .antMatchers("/actuator/**").permitAll()
	            .antMatchers("/api/auth/**").permitAll()
	            .antMatchers("/api/profile/**").hasRole("USER")
	            .anyRequest().authenticated()
            
	        .and()
            	.oauth2Login()
            
            .and()
		        .requiresChannel() // SSL
	 	        .anyRequest() // SSL
		        .requiresSecure() // SSL
            
		    .and()
            	.addFilter(jwtAuthorizationFilter());
    }
}
