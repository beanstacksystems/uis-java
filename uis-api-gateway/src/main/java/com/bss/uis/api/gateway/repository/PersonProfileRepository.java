package com.bss.uis.api.gateway.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bss.uis.api.gateway.model.PersonProfileEntity;

/**
 * @Author : Amran Hosssain on 6/24/2020
 */
public interface PersonProfileRepository extends JpaRepository<PersonProfileEntity,Long> {
}
