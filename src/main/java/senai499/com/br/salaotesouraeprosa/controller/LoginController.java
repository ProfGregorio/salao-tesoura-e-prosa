
package senai499.com.br.salaotesouraeprosa.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import senai499.com.br.salaotesouraeprosa.model.*;
import senai499.com.br.salaotesouraeprosa.repository.*;

@Controller
public class LoginController {

    private final UsuariosRepository usuariosRepository;
    private final ClientesRepository clientesRepository;

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public LoginController(
            UsuariosRepository usuariosRepository,
            ClientesRepository clientesRepository) {

        this.usuariosRepository = usuariosRepository;
        this.clientesRepository = clientesRepository;
    }

    // ==========================================
    // PÁGINA DE LOGIN
    // ==========================================

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ==========================================
    // AUTENTICAÇÃO
    // ==========================================

    @PostMapping("/login")
    public String autenticar(
            @RequestParam String email,
            @RequestParam String senha,
            HttpServletRequest request) {

        // Consultar o usuário pelo e-mail
        var usuarioOptional = usuariosRepository
                .findByEmailIgnoreCase(email.trim());

        if (usuarioOptional.isEmpty()) {
            return "redirect:/login?erro";
        }

        UsuariosModel usuario = usuarioOptional.get();

        // Verificar se está ativo
        if (!usuario.isAtivo()) {
            return "redirect:/login?inativo";
        }

        // Verificar senha usando BCrypt
        if (!encoder.matches(senha, usuario.getSenha())) {
            return "redirect:/login?erro";
        }

        // Verificar se possui perfil
        if (usuario.getPerfil() == null) {
            return "redirect:/login?erro";
        }

        // Dados do cliente, se for perfil CLIENTE
        ClientesModel cliente = null;

        if (usuario.getPerfil() == PerfilUsuario.CLIENTE) {

            cliente = clientesRepository
                    .findByUsuarioId(usuario.getId())
                    .orElse(null);

            if (cliente == null) {
                return "redirect:/login?erro";
            }
        }

        // ======================================
        // CRIAR SESSÃO
        // ======================================

        // Invalidar sessão anterior, se existir
        HttpSession anterior = request.getSession(false);

        if (anterior != null) {
            anterior.invalidate();
        }

        // Criar nova sessão
        HttpSession session = request.getSession(true);

        session.setAttribute("autenticado", true);
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("nomeUsuario", usuario.getEmail());
        session.setAttribute("perfil", usuario.getPerfil());

        // Sessão expira após 30 minutos de inatividade
        session.setMaxInactiveInterval(30 * 60);

        // ======================================
        // REDIRECIONAMENTO POR PERFIL
        // ======================================

        switch (usuario.getPerfil()) {

            case ADMIN:

                return "redirect:/dashboard";

            case CLIENTE:

                session.setAttribute(
                        "clienteId",
                        cliente.getId()
                );

                session.setAttribute(
                        "nomeUsuario",
                        cliente.getNome()
                );

                //return "redirect:/agendamentos/novo";
                return "redirect:/cliente/painel";

            case FUNCIONARIO:

                return "redirect:/admin/funcionarios/";

            default:

                session.invalidate();
                return "redirect:/login?erro";
        }
    }

    // ==========================================
    // LOGOUT
    // ==========================================

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }
}
