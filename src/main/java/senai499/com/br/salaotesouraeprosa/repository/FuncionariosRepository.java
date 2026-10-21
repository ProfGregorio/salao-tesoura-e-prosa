package senai499.com.br.salaotesouraeprosa.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import senai499.com.br.salaotesouraeprosa.model.FuncionariosModel;

public interface FuncionariosRepository
        extends JpaRepository<FuncionariosModel, Long> {

    Optional<FuncionariosModel> findByUsuario_Id(Long usuarioId);

    @Query("""
        SELECT DISTINCT f
        FROM FuncionariosModel f
        JOIN f.servicos s
        WHERE s.id = :servicoId
          AND s.ativo = true
          AND f.usuario.ativo = true
        ORDER BY f.nome
    """)
    List<FuncionariosModel> listarPorServico(
            @Param("servicoId") Long servicoId);
}