package com.github.al.realworld.api.query;

import com.github.al.realworld.api.ProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GetProfileResult {

    private ProfileDto profile;

}