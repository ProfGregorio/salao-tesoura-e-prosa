
package senai499.com.br.salaotesouraeprosa.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import senai499.com.br.salaotesouraeprosa.model.*;

public interface AgendamentosRepository
        extends JpaRepository<AgendamentosModel, Long> {

    // Total de agendamentos do cliente
    long countByCliente_Id(Long clienteId);

    // Total por status
    long countByCliente_IdAndStatus(
            Long clienteId,
            StatusAgendamento status
    );

    // Próximos cinco atendimentos
    List<AgendamentosModel>
    findTop5ByCliente_IdAndDataHoraGreaterThanEqualOrderByDataHoraAsc(
            Long clienteId,
            LocalDateTime dataHora
    );

    // Evitar solicitações duplicadas do mesmo cliente
    boolean existsByCliente_IdAndDataHoraAndStatusNot(
            Long clienteId,
            LocalDateTime dataHora,
            StatusAgendamento status
    );
}
