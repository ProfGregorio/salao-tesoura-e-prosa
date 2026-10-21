package senai499.com.br.salaotesouraeprosa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import senai499.com.br.salaotesouraeprosa.model.ClientesModel;

public interface ClientesRepository
        extends JpaRepository<ClientesModel, Long> {

    Optional<ClientesModel> findByUsuarioId(Long usuarioId);

    boolean existsByCpf(String cpf);
}