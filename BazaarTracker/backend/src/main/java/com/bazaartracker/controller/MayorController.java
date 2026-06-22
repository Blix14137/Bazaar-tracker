package com.bazaartracker.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bazaartracker.model.MayorData;
import com.bazaartracker.service.MayorService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MayorController {

    private final MayorService mayorService =
            new MayorService();

    @GetMapping("/mayor")
    public MayorData getCurrentMayor() {

        return mayorService.getCurrentMayor();
    }
}
