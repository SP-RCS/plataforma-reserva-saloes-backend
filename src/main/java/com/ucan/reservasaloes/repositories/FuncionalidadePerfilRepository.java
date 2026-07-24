package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.FuncionalidadePerfil;
import com.ucan.reservasaloes.entities.Perfil;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface FuncionalidadePerfilRepository extends JpaRepository<FuncionalidadePerfil, Integer> {

       @Query("SELECT COUNT(fp) FROM FuncionalidadePerfil fp WHERE fp.fkPerfil = :perfil")
    Long countByFkPerfil(@Param("perfil") Perfil perfil);
    
    List<FuncionalidadePerfil> findByFkPerfil(Perfil perfil);
Optional<FuncionalidadePerfil> 
findByFkPerfilPkPerfilAndFkFuncionalidadePkFuncionalidade(Integer fkPerfil, Integer fkFuncionalidade);

    List<FuncionalidadePerfil> findByFkPerfil_PkPerfil(Integer pkPerfil);
    
        boolean existsByFkPerfil_PkPerfilAndFkFuncionalidade_PkFuncionalidade(Integer perfilId, Integer funcionalidadeId);

        
}


