package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.Telefone;
import java.util.List;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface TelefoneRepository extends JpaRepository<Telefone, Integer> {
    @Query("SELECT t FROM Telefone t WHERE t.fkPessoa.pkPessoa = :pessoaId AND t.principal = true")
    Telefone findPrincipalByPessoaId(@Param("pessoaId") Integer pessoaId);
    
    List<Telefone> findByFkPessoaPkPessoa(Integer pessoaId);


}
