package com.ucan.reservasaloes.services;

import com.ucan.reservasaloes.entities.FuncionalidadePerfil;
import com.ucan.reservasaloes.repositories.FuncionalidadePerfilRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Sebastiao
 */
@Service
public class FuncionalidadePerfilService {

    @Autowired
    private FuncionalidadePerfilRepository repository;

    public FuncionalidadePerfil salvar(FuncionalidadePerfil obj) {
        return repository.save(obj);
    }

    public List<FuncionalidadePerfil> listarTodos() {
        return repository.findAll();
    }

    public Optional<FuncionalidadePerfil> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public void remover(Integer id) {
        repository.deleteById(id);
    }
}
