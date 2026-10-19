package senai499.com.br.salaotesouraeprosa.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CoreController {

    @GetMapping("/clientes")
    public String clientes(HttpSession session) {

        // Verifica se o usuário está autenticado
        if (!Boolean.TRUE.equals(
                session.getAttribute("autenticado"))) {

            return "redirect:/login";
        }

        // Usuário autenticado: exibe a página
        return "clientes";
    }
}

