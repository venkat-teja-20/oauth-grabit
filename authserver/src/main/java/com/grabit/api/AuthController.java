package com.grabit.api;

import com.grabit.bean.LoginRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping(value = "/auth")
@Log4j2
public class AuthController {

    private final JwtEncoder jwtEncoder;

    public AuthController(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping(value = "/login",consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object login(@RequestBody LoginRequest request, HttpServletResponse response){
        try{
            if("123456".equals(request.getOtp())){
                response.setStatus(200);
                String accessToken=getAccessToken(request.getMobileNumber());
                return ResponseEntity.status(200).body(Map.of("message","Login Successful","access_token",accessToken));
            }
            return ResponseEntity.status(401).body(Map.of("message","Login unsuccessful"));
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity
                    .status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .body(Map.of("code","SOMETHING_WENT_WRONG","message",e.getMessage()));
        }
    }

    private String getAccessToken(String mobileNumber){
        Instant t=Instant.now();
        JwtClaimsSet jwtClaimsSet=JwtClaimsSet.builder()
                .subject(mobileNumber)
                .issuedAt(t)
                .expiresAt(t.plusSeconds(3600))
                .claim("type","access_token")
                .claim("access_level","member")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();
    }
}
