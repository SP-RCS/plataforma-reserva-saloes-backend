package com.ucan.reservasaloes.services;

import com.ucan.reservasaloes.dto.ContaPerfilDTO;
import com.ucan.reservasaloes.entities.ContaPerfil;
import com.ucan.reservasaloes.repositories.ContaPerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContaPerfilService {

    private final ContaPerfilRepository repository;

    public ContaPerfilService(ContaPerfilRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ContaPerfilDTO> listarContaPerfis() {
        return repository.findAllWithContaPessoa()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private ContaPerfilDTO toDTO(ContaPerfil cp) {
        ContaPerfilDTO dto = new ContaPerfilDTO();
        dto.setNomeCompleto(cp.getFkConta().getFkPessoa().getNome());
        dto.setEmail(cp.getFkConta().getEmail());
        dto.setTipoConta(cp.getFkConta().getTipoConta().name());
        dto.setEstado(cp.getEstado());
        return dto;
    }

    public Optional<ContaPerfil> buscarPorId(Integer id) {
        return repository.findById(id);
    }

    public ContaPerfil salvar(ContaPerfil entity) {
        return repository.save(entity);
    }

    public void remover(Integer id) {
        repository.deleteById(id);
    }
}
