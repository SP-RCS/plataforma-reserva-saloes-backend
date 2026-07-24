package com.ucan.reservasaloes.controllers;

import com.ucan.reservasaloes.entities.Localidade;
import com.ucan.reservasaloes.services.LocalidadeService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/localidades")
@CrossOrigin(origins = "*")
public class LocalidadeController {

    @Autowired
    private LocalidadeService service;

    // Listar todas as localidades
    @GetMapping
    public ResponseEntity<List<Localidade>> listarTodas() {
        List<Localidade> lista = service.listarTodas();
        return ResponseEntity.ok(lista);
    }

    // Buscar uma localidade pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Localidade> buscarPorId(@PathVariable Integer id) {
        Optional<Localidade> localidade = service.buscarPorId(id);
        return localidade.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    // Criar nova localidade
    @PostMapping
    public ResponseEntity<Localidade> criar(@RequestBody Localidade localidade) {
        Localidade nova = service.salvar(localidade);
        return ResponseEntity.ok(nova);
    }

    // Atualizar localidade
    @PutMapping("/{id}")
    public ResponseEntity<Localidade> atualizar(@PathVariable Integer id, @RequestBody Localidade localidade) {
        Optional<Localidade> existente = service.buscarPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        localidade.setPkLocalidade(id);
        Localidade atualizada = service.salvar(localidade);
        return ResponseEntity.ok(atualizada);
    }

    // Deletar localidade
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Listar localidades filhas (subdivisões)
    @GetMapping("/filhas/{idPai}")
    public ResponseEntity<List<Localidade>> listarPorLocalidadePai(@PathVariable Integer idPai) {
        List<Localidade> filhas = service.listarPorLocalidadePai(idPai);
        return ResponseEntity.ok(filhas);
    }
}
