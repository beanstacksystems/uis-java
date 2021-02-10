package com.bss.uis.api.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bss.uis.api.gateway.model.UserEntity;

/**
 * @Author : Amran Hosssain on 6/23/2020
 */
public interface UserRepository extends JpaRepository<UserEntity,Long> {

    UserEntity findByUserNameAndStatus(String userName, String status);

    UserEntity findByUserName(String userName);
}
