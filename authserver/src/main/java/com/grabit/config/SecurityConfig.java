package com.grabit.config;

import com.grabit.handler.CustomAuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

@Configuration
public class SecurityConfig {


    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests-> requests
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
        ;
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(){
        UserDetails userDetails= User.builder()
                .username("user")
                .password("{noop}password")
                .roles("user")
                .build();
        return new InMemoryUserDetailsManager(userDetails);
    }

//    @Bean
//    public RequestCache requestCache() {
//        return new HttpSessionRequestCache();
//    }
}
