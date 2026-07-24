package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.Conta;
import com.ucan.reservasaloes.entities.Pessoa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Integer> {

    public Conta findByPkConta(Integer pkConta);

    Optional<Conta> findByEmail(String email);
    
    boolean existsByEmail(String email);
   
    Long countByFkPessoa(Pessoa pessoa);

    public Conta findByEmailAndPasswordHash(String email, String passwordHash);

    @Query("""
    SELECT DISTINCT c
    FROM Conta c
    JOIN FETCH c.contaPerfis cp
    JOIN FETCH cp.fkPerfil p
    JOIN FETCH p.funcionalidades fp
    JOIN FETCH fp.fkFuncionalidade
    WHERE c.email = :email 
    """)
    Optional<Conta> findContaComPerfisEFuncionalidadesByEmail(
            @Param("email") String email
    );
}
