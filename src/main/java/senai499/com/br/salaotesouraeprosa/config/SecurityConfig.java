package senai499.com.br.salaotesouraeprosa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // Páginas públicas
                        .requestMatchers(
                                "/",
                                "/login",
                                "/servicos",
                                "/css/**",
                                "/js/**",
                                "/img/**"
                        ).permitAll()

                        // Páginas protegidas
                        .requestMatchers(
                                "/clientes/**",
                                "/agendamentos/**",
                                "/perfil/**"
                        ).authenticated()

                        .anyRequest().permitAll()
                )

                // LOGIN PERSONALIZADO
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?erro")
                        .permitAll()
                )

                // LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?sair")
                        .permitAll()
                );

        return http.build();
    }
}
/*
* Dentro do método securityFilterChain, encontre:
.anyRequest().permitAll()
* Essa instrução permite acesso a todas as rotas que não foram tratadas anteriormente.
Por enquanto, mantenha assim, pois você ainda está construindo o sistema e quer acessar as páginas sem login.
Quando implementar a autenticação, poderá trocar por:
*
* .requestMatchers(
    "/clientes/**",
    "/agendamentos/**",
    "/perfil/**"
).authenticated()

.anyRequest().permitAll()
*
* */