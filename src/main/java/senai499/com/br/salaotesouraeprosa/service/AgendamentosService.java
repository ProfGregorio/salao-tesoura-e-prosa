
package senai499.com.br.salaotesouraeprosa.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import senai499.com.br.salaotesouraeprosa.dto.AgendamentoForm;
import senai499.com.br.salaotesouraeprosa.model.*;
import senai499.com.br.salaotesouraeprosa.repository.*;

@Service
public class AgendamentosService {

    private final AgendamentosRepository agendamentosRepository;
    private final ClientesRepository clientesRepository;

    public AgendamentosService(
            AgendamentosRepository agendamentosRepository,
            ClientesRepository clientesRepository) {

        this.agendamentosRepository = agendamentosRepository;
        this.clientesRepository = clientesRepository;
    }

    // CADASTRAR SOLICITAÇÃO
    @Transactional
    public void solicitar(
            Long usuarioId,
            AgendamentoForm form) {

        // Identificar cliente pela sessão autenticada
        ClientesModel cliente = clientesRepository
                .findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cliente não encontrado."
                        )
                );

        String servico = form.getServico() == null
                ? ""
                : form.getServico().trim();

        if (servico.isBlank() || servico.length() > 120) {
            throw new IllegalArgumentException(
                    "Informe um serviço válido."
            );
        }

        if (form.getDataHora() == null ||
                !form.getDataHora().isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Escolha uma data e horário futuros."
            );
        }

        String observacao = form.getObservacao() == null
                ? ""
                : form.getObservacao().trim();

        if (observacao.length() > 500) {
            throw new IllegalArgumentException(
                    "A observação deve ter até 500 caracteres."
            );
        }

        // Verificar solicitação existente no mesmo horário
        boolean duplicado = agendamentosRepository
                .existsByCliente_IdAndDataHoraAndStatusNot(
                        cliente.getId(),
                        form.getDataHora(),
                        StatusAgendamento.CANCELADO
                );

        if (duplicado) {
            throw new IllegalArgumentException(
                    "Você já possui um agendamento nesse horário."
            );
        }

        AgendamentosModel agendamento =
                new AgendamentosModel();

        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setDataHora(form.getDataHora());
        agendamento.setObservacao(observacao);
        agendamento.setStatus(StatusAgendamento.PENDENTE);

        agendamentosRepository.save(agendamento);
    }

    // ESTATÍSTICAS DO CLIENTE
    public long total(Long clienteId) {
        return agendamentosRepository.countByCliente_Id(clienteId);
    }

    public long pendentes(Long clienteId) {
        return agendamentosRepository.countByCliente_IdAndStatus(
                clienteId,
                StatusAgendamento.PENDENTE
        );
    }

    public long concluidos(Long clienteId) {
        return agendamentosRepository.countByCliente_IdAndStatus(
                clienteId,
                StatusAgendamento.CONCLUIDO
        );
    }

    // PRÓXIMOS AGENDAMENTOS
    public List<AgendamentosModel> proximos(Long clienteId) {

        return agendamentosRepository
                .findTop5ByCliente_IdAndDataHoraGreaterThanEqualOrderByDataHoraAsc(
                        clienteId,
                        LocalDateTime.now()
                );
    }
}
