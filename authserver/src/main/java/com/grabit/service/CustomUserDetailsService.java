package com.grabit.service;

import com.grabit.mapper.MemberURLMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberURLMapper memberURLMapper;

    private final RestTemplate restTemplate;

    public CustomUserDetailsService(@Qualifier("memberURLMapper") MemberURLMapper memberURLMapper, RestTemplate restTemplate) {
        this.memberURLMapper = memberURLMapper;
        this.restTemplate = restTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Map<String, Object> memberData = restTemplate.getForObject(memberURLMapper.getMemberDataURL(email), Map.class);
        log.info("Member Details Info : "+memberData.toString().getBytes().length);
        return User.withUsername(String.valueOf(memberData.get("email")))
                .password(String.valueOf(memberData.get("password")))
                .roles(String.valueOf(memberData.get("role")))
                .build();
    }
}
