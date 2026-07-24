package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.ContaPerfil;
import com.ucan.reservasaloes.entities.Perfil;
import com.ucan.reservasaloes.entities.Conta;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ContaPerfilRepository extends JpaRepository<ContaPerfil, Integer> {

    @Query("SELECT COUNT(cp) FROM ContaPerfil cp WHERE cp.fkPerfil = :perfil")
    Long countByFkPerfil(@Param("perfil") Perfil perfil);

    List<ContaPerfil> findByFkPerfil(Perfil perfil);

    @Query("""
                SELECT cp FROM ContaPerfil cp
                JOIN FETCH cp.fkPerfil
                WHERE cp.fkConta = :conta
            """)
    Optional<ContaPerfil> findByFkConta(Conta conta);

    List<ContaPerfil> findAllByFkConta(Conta conta);

    @Transactional
    @Modifying
    void deleteByFkConta(Conta conta);


    boolean existsByFkConta(Conta conta);

    @Query("""
                SELECT cp
                FROM ContaPerfil cp
                JOIN FETCH cp.fkConta c
                JOIN FETCH c.fkPessoa p
            """)
    List<ContaPerfil> findAllWithContaPessoa();
}
