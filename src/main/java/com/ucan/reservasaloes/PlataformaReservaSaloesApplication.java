package com.ucan.reservasaloes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.ucan.reservasaloes.config",
        "com.ucan.reservasaloes.controllers",
        "com.ucan.reservasaloes.dto",
        "com.ucan.reservasaloes.entities",
        "com.ucan.reservasaloes.enumerable",
        "com.ucan.reservasaloes.initializer",
        "com.ucan.reservasaloes.repositories",
        "com.ucan.reservasaloes.services",
        "com.ucan.reservasaloes.utils",
})
public class PlataformaReservaSaloesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlataformaReservaSaloesApplication.class, args);
    }

}
