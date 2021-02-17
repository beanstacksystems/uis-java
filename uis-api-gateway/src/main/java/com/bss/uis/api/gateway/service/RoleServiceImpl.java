package com.bss.uis.api.gateway.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bss.uis.api.gateway.model.UserRoleEntity;
import com.bss.uis.api.gateway.repository.RoleRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author : Amran Hosssain on 6/23/2020
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService{


    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<UserRoleEntity> getAllRoles() {
        log.info("getAllRoles method call...");
        return roleRepository.findAll();
    }
}
