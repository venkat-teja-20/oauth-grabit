package com.grabit.api;

import java.util.List;

public record ClientRegistrationRequest(
        String clientId,
        String clientSecret,
        List<String> grantTypes,
        List<String> scopes,
        List<String> redirectUris,
        boolean requirePkce,
        int accessTokenTTLMinutes,
        int refreshTokenTTLDays
) {}
