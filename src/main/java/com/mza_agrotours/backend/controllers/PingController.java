package com.mza_agrotours.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.status(200).body("pong");
    }
}