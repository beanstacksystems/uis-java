package com.bss.uis.api.gateway.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bss.uis.api.gateway.service.UserService;

/**
 * @Author : Amran Hosssain on 6/23/2020
 */
@Component
public class UserConfigMetaData implements Serializable {

	private static final long serialVersionUID = 1L;

	@Autowired
    private UserService userService;


    public Map<String, Object> getUserRelatedInformation(String userName) {
        Map<String, Object> maps = new HashMap<>();
//        UserEntity userEntity = userService.findByUserName(userName);
//        maps.put("user_role", userEntity.getUserRoleEntity().getRoleName());
//        maps.put("userName", userEntity.getUserName());
//        maps.put("fullName", userEntity.getPersonProfileEntity().getFirstName().concat(" ").concat(userEntity.getPersonProfileEntity().getLastName()));
//        maps.put("email", userEntity.getPersonProfileEntity().getEmail());
        return maps;
    }
}
