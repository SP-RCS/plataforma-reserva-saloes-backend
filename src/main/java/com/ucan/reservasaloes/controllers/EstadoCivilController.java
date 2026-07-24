package com.ucan.reservasaloes.controllers;

import com.ucan.reservasaloes.entities.EstadoCivil;
import com.ucan.reservasaloes.repositories.EstadoCivilRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Sebastiao
 */
@RestController
@RequestMapping("/api/estado_civil")
@CrossOrigin(origins = "*")
public class EstadoCivilController {

    @Autowired
    private EstadoCivilRepository civilRepository;

    @GetMapping("estado_civil_listar")
    public ResponseEntity<List<EstadoCivil>> estado_civil_listar() {
        List<EstadoCivil> estadoCivils = civilRepository.findAll();
        return ResponseEntity.ok(estadoCivils);
    }

   
}
