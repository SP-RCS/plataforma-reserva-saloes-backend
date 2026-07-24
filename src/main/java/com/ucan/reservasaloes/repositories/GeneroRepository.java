
package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.Genero;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Sebastiao
 */
@Repository
public interface GeneroRepository extends JpaRepository<Genero, Integer> {


    Optional<Genero> findByNome(String nome);

}

