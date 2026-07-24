package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.Localidade;
import com.ucan.reservasaloes.enumerable.TipoLocalidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalidadeRepository extends JpaRepository<Localidade, Integer> {
    
    @Query("SELECT l FROM Localidade l WHERE UPPER(l.nome) = UPPER(:nome)")
    Optional<Localidade> findByNome(@Param("nome") String nome);
    
    // Método para buscar TODAS as localidades com um nome específico (case-insensitive)
    @Query("SELECT l FROM Localidade l WHERE UPPER(l.nome) = UPPER(:nome)")
    List<Localidade> findAllByNome(@Param("nome") String nome);
    
    Optional<Localidade> findByNomeAndLocalidadePai(String nome, Localidade localidadePai);
    
    // Buscar localidade por nome e tipo
    @Query("SELECT l FROM Localidade l WHERE UPPER(l.nome) = UPPER(:nome) AND l.tipo = :tipo")
    Optional<Localidade> findByNomeAndTipo(@Param("nome") String nome, @Param("tipo") TipoLocalidade tipo);
    
    List<Localidade> findByTipo(TipoLocalidade tipo);
    
    List<Localidade> findByLocalidadePai(Localidade localidadePai);
    
    @Query("SELECT l FROM Localidade l WHERE l.tipo = 'MUNICIPIO'")
    List<Localidade> findAllMunicipios();
    
    @Query("SELECT l FROM Localidade l WHERE l.tipo = 'BAIRRO' AND l.localidadePai.nome = :nomeMunicipio")
    List<Localidade> findBairrosByMunicipio(@Param("nomeMunicipio") String nomeMunicipio);
}
