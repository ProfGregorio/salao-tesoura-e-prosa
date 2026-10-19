package senai499.com.br.salaotesouraeprosa.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String autenticar(
            @RequestParam String usuario,
            @RequestParam String senha,
            HttpSession session) {

        // Apenas para demonstração didática
        if (usuario.equals("admin") && senha.equals("1234")) {

            session.setAttribute("autenticado", true);
            session.setAttribute("nomeUsuario", usuario);

            return "redirect:/";
        }

        return "redirect:/login?erro";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        //return "redirect:/login?sair";
        // Redireciona para a página inicial
        return "redirect:/";
    }
}