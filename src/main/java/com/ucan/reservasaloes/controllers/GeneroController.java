package com.ucan.reservasaloes.controllers;

import com.ucan.reservasaloes.entities.Genero;
import com.ucan.reservasaloes.repositories.GeneroRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Sebastiao
 */
@RestController
@RequestMapping("/api/genero")
@CrossOrigin(origins = "*")
public class GeneroController {

    @Autowired
    private GeneroRepository generoRepository;

    @GetMapping("genero_listar")
    public ResponseEntity<List<Genero>> genero_listar() {
        List<Genero> generos = generoRepository.findAll();
        return ResponseEntity.ok(generos);
    }

   
}
