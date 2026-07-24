package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Integer> {
    Perfil findByPkPerfil(Integer pkPerfil);
    boolean existsByDesignacaoIgnoreCase(String designacao);
    
    @Query("SELECT COUNT(p) > 0 FROM Perfil p WHERE LOWER(p.designacao) = LOWER(:designacao) AND p.pkPerfil != :pkPerfil")
    boolean existsByDesignacaoIgnoreCaseAndPkPerfilNot(@Param("designacao") String designacao, @Param("pkPerfil") Integer pkPerfil);
}
