package com.grabit.mapper;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PartnerURLMapperBuilder {
    @Builder(builderMethodName = "builder")
    private static MemberURLMapper memberURLMapper(String routerURL){
        return new MemberURLMapper(routerURL);
    }
}
