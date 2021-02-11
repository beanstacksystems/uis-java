package com.bss.uis.api.gateway.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bss.uis.api.gateway.util.CommonResponse;

@RestController
@RequestMapping("/api")
public class LogOutController {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogOutController.class);

    @GetMapping(path = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommonResponse> userLogout(@RequestParam String userName, Principal principal) {
        LOGGER.info("Request Received for userLogout() ...");
        return new ResponseEntity<>(new CommonResponse("User Logout Successful.",true,null,HttpStatus.OK.value()), HttpStatus.ACCEPTED);
    }
}
