package com.github.al.realworld.api.command;

import com.github.al.realworld.api.ProfileDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnfollowProfileResult {

    private ProfileDto profile;

}
