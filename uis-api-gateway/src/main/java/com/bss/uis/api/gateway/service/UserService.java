package com.bss.uis.api.gateway.service;

import com.bss.uis.api.gateway.dto.UserSignupDTO;
import com.bss.uis.api.gateway.model.UserEntity;

/**
 * @Author : Amran Hosssain on 6/23/2020
 */
public interface UserService {

    String signup(UserSignupDTO userSignupDTO);

    UserEntity findByUserName(String userName);
}
