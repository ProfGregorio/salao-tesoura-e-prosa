package senai499.com.br.salaotesouraeprosa.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import senai499.com.br.salaotesouraeprosa.model.UsuariosModel;
import senai499.com.br.salaotesouraeprosa.repository.UsuariosRepository;

@Service
public class UsuariosService {

    private final UsuariosRepository usuariosRepository;

    // Responsável pela criptografia (hash) das senhas
    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder();

    public UsuariosService(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    // CADASTRAR USUÁRIO
    public UsuariosModel cadastrar(UsuariosModel usuario) {

        // Converte a senha informada em um hash
        usuario.setSenha(
                encoder.encode(usuario.getSenha())
        );

        // Salva o usuário no banco de dados
        return usuariosRepository.save(usuario);
    }
}