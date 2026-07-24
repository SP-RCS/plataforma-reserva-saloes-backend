package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.Funcionalidade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FuncionalidadeRepository extends JpaRepository<Funcionalidade, Integer> {
// metodo usado no menu
    //  @Query("SELECT f FROM Funcionalidade f JOIN FuncionalidadePerfil pf ON pf.fkFuncionalidade.pkFuncionalidade = f.pkFuncionalidade WHERE pf.fkPerfil.pkPerfil = :pkPerfil and f.fkTipoFuncionalidade.pkTipoFuncionalidade >= 7") 

    @Query("""
    SELECT f FROM Funcionalidade f
    LEFT JOIN FETCH f.fkFuncionalidadePai
    JOIN f.perfis fp
    WHERE fp.fkPerfil.pkPerfil = :pkPerfil
""")
    List findByPerfil(@Param("pkPerfil") Integer pkPerfil);

    // Métodos existentes...
    public Funcionalidade findByPkFuncionalidade(Integer pkFuncionalidade);

    // VALIDAÇÃO ANTIGA: descricao + designacao + pai (mantida para compatibilidade)
    @Query("SELECT f FROM Funcionalidade f WHERE f.descricao = :descricao AND f.designacao = :designacao AND f.fkFuncionalidadePai.pkFuncionalidade = :paiId")
    List<Funcionalidade> findByDescricaoAndDesignacaoAndFkFuncionalidadePaiPkFuncionalidade(
            @Param("descricao") String descricao,
            @Param("designacao") String designacao,
            @Param("paiId") Integer paiId
    );

    @Query("SELECT f FROM Funcionalidade f WHERE f.descricao = :descricao AND f.designacao = :designacao AND f.fkFuncionalidadePai IS NULL")
    List<Funcionalidade> findByDescricaoAndDesignacaoAndFkFuncionalidadePaiIsNull(
            @Param("descricao") String descricao,
            @Param("designacao") String designacao
    );

    @Query("SELECT f FROM Funcionalidade f WHERE f.descricao = :descricao AND f.designacao = :designacao AND f.fkFuncionalidadePai.pkFuncionalidade = :paiId AND f.pkFuncionalidade != :excluirId")
    List<Funcionalidade> findByDescricaoAndDesignacaoAndFkFuncionalidadePaiPkFuncionalidadeAndPkFuncionalidadeNot(
            @Param("descricao") String descricao,
            @Param("designacao") String designacao,
            @Param("paiId") Integer paiId,
            @Param("excluirId") Integer excluirId
    );

    @Query("SELECT f FROM Funcionalidade f WHERE f.descricao = :descricao AND f.designacao = :designacao AND f.fkFuncionalidadePai IS NULL AND f.pkFuncionalidade != :excluirId")
    List<Funcionalidade> findByDescricaoAndDesignacaoAndFkFuncionalidadePaiIsNullAndPkFuncionalidadeNot(
            @Param("descricao") String descricao,
            @Param("designacao") String designacao,
            @Param("excluirId") Integer excluirId
    );

  // 1. Buscar por designação e pai específico (para cadastro)
    @Query("SELECT f FROM Funcionalidade f WHERE "
            + "LOWER(TRIM(f.designacao)) = LOWER(TRIM(:designacao)) AND "
            + "f.fkFuncionalidadePai.pkFuncionalidade = :fkPaiId")
    List<Funcionalidade> findByDesignacaoAndFkFuncionalidadePaiPkFuncionalidade(
            @Param("designacao") String designacao,
            @Param("fkPaiId") Integer fkPaiId);

    // 2. Buscar por designação e sem pai (pai é null) - para cadastro
    @Query("SELECT f FROM Funcionalidade f WHERE "
            + "LOWER(TRIM(f.designacao)) = LOWER(TRIM(:designacao)) AND "
            + "f.fkFuncionalidadePai IS NULL")
    List<Funcionalidade> findByDesignacaoAndFkFuncionalidadePaiIsNull(
            @Param("designacao") String designacao);

    // 3. Buscar por designação e pai específico 
    @Query("SELECT f FROM Funcionalidade f WHERE "
            + "LOWER(TRIM(f.designacao)) = LOWER(TRIM(:designacao)) AND "
            + "f.fkFuncionalidadePai.pkFuncionalidade = :fkPaiId AND "
            + "f.pkFuncionalidade != :excluirId")
    List<Funcionalidade> findByDesignacaoAndFkFuncionalidadePaiPkFuncionalidadeAndPkFuncionalidadeNot(
            @Param("designacao") String designacao,
            @Param("fkPaiId") Integer fkPaiId,
            @Param("excluirId") Integer excluirId);

    // 4. Buscar por designação e sem pai 
    @Query("SELECT f FROM Funcionalidade f WHERE "
            + "LOWER(TRIM(f.designacao)) = LOWER(TRIM(:designacao)) AND "
            + "f.fkFuncionalidadePai IS NULL AND "
            + "f.pkFuncionalidade != :excluirId")
    List<Funcionalidade> findByDesignacaoAndFkFuncionalidadePaiIsNullAndPkFuncionalidadeNot(
            @Param("designacao") String designacao,
            @Param("excluirId") Integer excluirId);

    
    @Query("SELECT COUNT(f) > 0 FROM Funcionalidade f WHERE "
            + "LOWER(TRIM(f.designacao)) = LOWER(TRIM(:designacao)) AND "
            + "(f.fkFuncionalidadePai.pkFuncionalidade = :fkPaiId OR "
            + "(f.fkFuncionalidadePai IS NULL AND :fkPaiId IS NULL))")
    boolean existsByDesignacaoAndFkFuncionalidadePai(
            @Param("designacao") String designacao,
            @Param("fkPaiId") Integer fkPaiId);

   
    @Query("SELECT COUNT(f) > 0 FROM Funcionalidade f WHERE "
            + "LOWER(TRIM(f.designacao)) = LOWER(TRIM(:designacao)) AND "
            + "((f.fkFuncionalidadePai.pkFuncionalidade = :fkPaiId AND :fkPaiId IS NOT NULL) OR "
            + "(f.fkFuncionalidadePai IS NULL AND :fkPaiId IS NULL))")
    boolean existsByDesignacaoAndFkFuncionalidadePaiId(
            @Param("designacao") String designacao,
            @Param("fkPaiId") Integer fkPaiId);
List<Funcionalidade> findByFkFuncionalidadePai_PkFuncionalidade(Integer pkFuncionalidadePai);

}
