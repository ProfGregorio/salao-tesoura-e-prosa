package senai499.com.br.salaotesouraeprosa.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import senai499.com.br.salaotesouraeprosa.model.ServicosModel;

public interface ServicosRepository
        extends JpaRepository<ServicosModel, Long> {

    List<ServicosModel> findByAtivoTrueOrderByNomeAsc();
}