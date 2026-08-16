package com.grabit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    @Order(3)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        .anyRequest().authenticated())
                .formLogin(Customizer.withDefaults());
        return http.build();
    }

//    @Bean
//    public UserDetailsService userDetailsService(){
//        UserDetails userDetails= User.builder()
//                .username("user")
//                .password("{noop}password")
//                .roles("user")
//                .build();
//        return new InMemoryUserDetailsManager(userDetails);
//    }

//    @Bean
//    public RequestCache requestCache() {
//        return new HttpSessionRequestCache();
//    }
}
