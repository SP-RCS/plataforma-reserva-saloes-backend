package com.ucan.reservasaloes.services;
import com.ucan.reservasaloes.entities.Pessoa;
import com.ucan.reservasaloes.repositories.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author Sebastiao
 */

@Service
public class PessoaService {

    @Autowired
    private PessoaRepository pessoaRepository;

    public List<Pessoa> listarTodas() {
        return pessoaRepository.findAll();
    }

    public Optional<Pessoa> buscarPorId(Integer id) {
        return pessoaRepository.findById(id);
    }

    public Pessoa salvar(Pessoa pessoa) {
        return pessoaRepository.save(pessoa);
    }

    public void deletar(Integer id) {
        pessoaRepository.deleteById(id);
    }
}
