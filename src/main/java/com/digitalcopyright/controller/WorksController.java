package com.digitalcopyright.controller;

import com.digitalcopyright.model.DO.WorksDO;
import org.fisco.bcos.sdk.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/works")
public class WorksController {
    @Autowired
    private Client client;

    @PostMapping("/registerWorks")
    public String registerWorks(@RequestBody WorksDO worksDO) {
        return "Hello, World!";
    }
}
