package com.ucan.reservasaloes.controllers;

import com.ucan.reservasaloes.services.LocalidadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de endereço esperados pelo ContaCadastrar do frontend.
 * Mapeia a hierarquia de Localidade (Angola → província → município → bairro).
 */
@RestController
@RequestMapping("/api/endereco")
@CrossOrigin(origins = "*")
public class EnderecoController {

    @Autowired
    private LocalidadeService localidadeService;

    @GetMapping("/provincia_listar")
    public ResponseEntity<List<Map<String, Object>>> listarProvincias() {
        return ResponseEntity.ok(localidadeService.listarProvinciasDto());
    }

    @GetMapping("/municipio_listar")
    public ResponseEntity<List<Map<String, Object>>> listarMunicipios() {
        return ResponseEntity.ok(localidadeService.listarMunicipiosDto());
    }

    @GetMapping("/bairro_listar")
    public ResponseEntity<List<Map<String, Object>>> listarBairros() {
        return ResponseEntity.ok(localidadeService.listarBairrosDto());
    }
}
