package com.bss.uis.api.gateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bss.uis.api.gateway.model.User;

/**
 * @Author : Amran Hosssain on 6/23/2020
 */
public interface UserRepository extends JpaRepository<User,Long> {

	User findByUserNameAndStatus(String userName, String status);

	User findByUserName(String userName);
    
	Optional<User> findByEmail(String email);
}
