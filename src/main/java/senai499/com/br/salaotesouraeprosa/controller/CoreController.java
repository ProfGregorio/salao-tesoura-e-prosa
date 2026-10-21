
package senai499.com.br.salaotesouraeprosa.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import senai499.com.br.salaotesouraeprosa.dto.*;
import senai499.com.br.salaotesouraeprosa.model.*;
import senai499.com.br.salaotesouraeprosa.service.*;

@Controller
public class CoreController {

    private final ClientesService clientesService;
    private final AgendamentosService agendamentosService;

    public CoreController(
            ClientesService clientesService,
            AgendamentosService agendamentosService) {

        this.clientesService = clientesService;
        this.agendamentosService = agendamentosService;
    }

    // ==========================================
    // CADASTRO PÚBLICO
    // ==========================================

    @GetMapping("/cadastro")
    public String formulario(Model model) {

        model.addAttribute("form", new CadastroClienteForm());

        return "clientes/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @ModelAttribute("form") CadastroClienteForm form,
            BindingResult resultado,
            Model model) {

        if (resultado.hasErrors()) {
            model.addAttribute("erro", "Confira os dados informados.");
            form.setSenha("");
            form.setConfirmarSenha("");
            return "clientes/cadastro";
        }

        try {
            clientesService.cadastrar(form);
            return "redirect:/login?cadastro=sucesso";

        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());

        } catch (DataIntegrityViolationException e) {
            model.addAttribute("erro", "CPF ou e-mail já cadastrado.");
        }

        form.setSenha("");
        form.setConfirmarSenha("");

        return "clientes/cadastro";
    }

    // ==========================================
    // PAINEL DO CLIENTE
    // ==========================================

    @GetMapping("/cliente/painel")
    public String painelCliente(
            HttpSession session,
            Model model) {

        String bloqueio = verificarCliente(session);

        if (bloqueio != null) {
            return bloqueio;
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        ClientesModel cliente =
                clientesService.buscarPorUsuario(usuarioId);

        Long clienteId = cliente.getId();

        // Dados cadastrais
        model.addAttribute("cliente", cliente);

        // Estatísticas
        model.addAttribute(
                "total",
                agendamentosService.total(clienteId)
        );

        model.addAttribute(
                "pendentes",
                agendamentosService.pendentes(clienteId)
        );

        model.addAttribute(
                "concluidos",
                agendamentosService.concluidos(clienteId)
        );

        // Lista dos próximos serviços
        model.addAttribute(
                "proximos",
                agendamentosService.proximos(clienteId)
        );

        return "clientes/index";
    }

    // ==========================================
    // EDITAR PERFIL
    // ==========================================

    @GetMapping("/cliente/perfil")
    public String editarPerfil(
            HttpSession session,
            Model model) {

        String bloqueio = verificarCliente(session);

        if (bloqueio != null) {
            return bloqueio;
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        model.addAttribute(
                "form",
                clientesService.carregarPerfil(usuarioId)
        );

        return "clientes/perfil";
    }

    @PostMapping("/cliente/perfil")
    public String atualizarPerfil(
            @ModelAttribute("form") PerfilClienteForm form,
            BindingResult resultado,
            HttpSession session,
            Model model) {

        String bloqueio = verificarCliente(session);

        if (bloqueio != null) {
            return bloqueio;
        }

        if (resultado.hasErrors()) {
            model.addAttribute("erro", "Confira os campos informados.");
            return "clientes/perfil";
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        try {
            ClientesModel cliente =
                    clientesService.atualizarPerfil(usuarioId, form);

            // Atualizar nome exibido no menu
            session.setAttribute("nomeUsuario", cliente.getNome());

            return "redirect:/cliente/painel?atualizado";

        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());

        } catch (DataIntegrityViolationException e) {
            model.addAttribute("erro", "CPF ou e-mail já cadastrado.");
        }

        return "clientes/perfil";
    }

    // ==========================================
    // AGENDAMENTOS
    // ==========================================

    @GetMapping("/agendamentos/novo")
    public String novoAgendamento(
            HttpSession session,
            Model model) {

        String bloqueio = verificarCliente(session);

        if (bloqueio != null) {
            return bloqueio;
        }

        model.addAttribute("form", new AgendamentoForm());

        return "agendamentos/agendamento";
    }

    @PostMapping("/agendamentos/novo")
    public String solicitarAgendamento(
            @ModelAttribute("form") AgendamentoForm form,
            BindingResult resultado,
            HttpSession session,
            Model model) {

        String bloqueio = verificarCliente(session);

        if (bloqueio != null) {
            return bloqueio;
        }

        if (resultado.hasErrors()) {
            model.addAttribute(
                    "erro",
                    "Confira a data e os dados do serviço."
            );

            return "agendamentos/agendamento";
        }

        Long usuarioId = (Long) session.getAttribute("usuarioId");

        try {
            agendamentosService.solicitar(usuarioId, form);

            return "redirect:/cliente/painel?agendado";

        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            return "agendamentos/agendamento";
        }
    }

    // ==========================================
    // ÁREA ADMINISTRATIVA
    // ==========================================

    @GetMapping("/clientes")
    public String clientes(HttpSession session) {

        if (!autenticado(session)) {
            return "redirect:/login";
        }

        if (session.getAttribute("perfil") != PerfilUsuario.ADMIN) {
            return "redirect:/acesso-negado";
        }

        return "clientes/index";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {

        if (!autenticado(session)) {
            return "redirect:/login";
        }

        if (session.getAttribute("perfil") != PerfilUsuario.ADMIN) {
            return "redirect:/acesso-negado";
        }

        return "admin/dashboard";
    }

    // ==========================================
    // FUNCIONÁRIOS
    // ==========================================

    @GetMapping("/funcionario")
    public String funcionario(HttpSession session) {

        if (!autenticado(session)) {
            return "redirect:/login";
        }

        if (session.getAttribute("perfil") != PerfilUsuario.FUNCIONARIO) {
            return "redirect:/acesso-negado";
        }

        // Temporário
        return "admin/funcionarios/index";
    }

    // ==========================================
    // ACESSO NEGADO
    // ==========================================

    @GetMapping("/acesso-negado")
    public String acessoNegado() {
        // Temporário
        return "index";
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private boolean autenticado(HttpSession session) {

        return Boolean.TRUE.equals(
                session.getAttribute("autenticado")
        );
    }

    private String verificarCliente(HttpSession session) {

        if (!autenticado(session)) {
            return "redirect:/login";
        }

        if (session.getAttribute("perfil") != PerfilUsuario.CLIENTE) {
            return "redirect:/acesso-negado";
        }

        return null;
    }
}
