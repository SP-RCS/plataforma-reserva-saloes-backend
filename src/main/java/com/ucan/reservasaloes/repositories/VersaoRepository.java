package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.Versao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VersaoRepository extends JpaRepository<Versao, Integer> {
    
    Optional<Versao> findByNomeTabela(String nomeTabela);
    
}
