package senai499.com.br.salaotesouraeprosa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import senai499.com.br.salaotesouraeprosa.model.UsuariosModel;

public interface UsuariosRepository
        extends JpaRepository<UsuariosModel, Long> {

    Optional<UsuariosModel> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}