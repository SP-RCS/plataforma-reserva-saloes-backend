package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.Pessoa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Integer> {

    public Pessoa findByPkPessoa(Integer pkPessoa);
    
    Optional<Pessoa> findByNome(String nome);
    
    @Query("SELECT p FROM Pessoa p WHERE p.nome = :nome")
    Pessoa findByNomeExato(@Param("nome") String nome);
    
    @Query("SELECT p FROM Pessoa p WHERE LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Pessoa> findByNomeContendo(@Param("nome") String nome);
    
    Optional<Pessoa> findByIdentificacao(String identificacao);
    
    boolean existsByIdentificacao(String identificacao);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Pessoa p WHERE p.identificacao = :identificacao")
    boolean verificarIdentificacaoExistente(@Param("identificacao") String identificacao);

}
