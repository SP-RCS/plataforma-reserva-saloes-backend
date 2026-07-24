
package com.ucan.reservasaloes.repositories;

import com.ucan.reservasaloes.entities.EstadoCivil;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Sebastiao
 */
@Repository
public interface EstadoCivilRepository extends JpaRepository<EstadoCivil, Integer> {


    Optional<EstadoCivil> findByNome(String nome);

}

