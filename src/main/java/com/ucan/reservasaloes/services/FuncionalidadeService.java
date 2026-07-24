package com.ucan.reservasaloes.services;

import com.ucan.reservasaloes.entities.Funcionalidade;
import com.ucan.reservasaloes.repositories.FuncionalidadeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Sebastiao
 */
@Service
public class FuncionalidadeService {

    @Autowired
    private FuncionalidadeRepository repository;

    public Funcionalidade salvar(Funcionalidade func) {
        return repository.save(func);
    }

    public List<Funcionalidade> listarTodos() {
        return repository.findAll();
    }

    public Optional<Funcionalidade> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public void remover(Integer id) {
        repository.deleteById(id);
    }
}
