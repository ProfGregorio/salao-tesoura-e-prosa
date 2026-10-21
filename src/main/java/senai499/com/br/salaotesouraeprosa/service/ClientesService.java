
package senai499.com.br.salaotesouraeprosa.service;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import senai499.com.br.salaotesouraeprosa.dto.CadastroClienteForm;
import senai499.com.br.salaotesouraeprosa.model.*;
import senai499.com.br.salaotesouraeprosa.repository.*;
import senai499.com.br.salaotesouraeprosa.dto.PerfilClienteForm;

@Service
public class ClientesService {

    private final ClientesRepository clientesRepository;
    private final UsuariosRepository usuariosRepository;
    private final UsuariosService usuariosService;

    public ClientesService(
            ClientesRepository clientesRepository,
            UsuariosRepository usuariosRepository,
            UsuariosService usuariosService) {

        this.clientesRepository = clientesRepository;
        this.usuariosRepository = usuariosRepository;
        this.usuariosService = usuariosService;
    }

    @Transactional
    public void cadastrar(CadastroClienteForm form) {

        String nome = texto(form.getNome());
        String email = texto(form.getEmail())
                .toLowerCase(Locale.ROOT);
        String cpf = somenteNumeros(form.getCpf());
        String cep = somenteNumeros(form.getCep());

        // Validações básicas
        if (nome.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o nome e o e-mail.");
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Informe um e-mail válido.");
        }

        if (cpf.length() != 11) {
            throw new IllegalArgumentException(
                    "O CPF deve conter 11 dígitos.");
        }

        if (cep.length() != 8) {
            throw new IllegalArgumentException(
                    "O CEP deve conter 8 dígitos.");
        }

        if (form.getDataNasc() == null ||
                form.getDataNasc().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Informe uma data de nascimento válida.");
        }

        if (form.getSenha() == null ||
                form.getSenha().length() < 8) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 8 caracteres.");
        }

        if (!form.getSenha().equals(form.getConfirmarSenha())) {
            throw new IllegalArgumentException(
                    "As senhas informadas são diferentes.");
        }

        if (texto(form.getLogradouro()).isBlank() ||
                texto(form.getNumero()).isBlank() ||
                texto(form.getBairro()).isBlank() ||
                texto(form.getCidade()).isBlank() ||
                !texto(form.getEstado()).matches("[A-Za-z]{2}")) {

            throw new IllegalArgumentException(
                    "Preencha corretamente o endereço.");
        }

        // Verificar duplicidade
        if (usuariosRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Este e-mail já está cadastrado.");
        }

        if (clientesRepository.existsByCpf(cpf)) {
            throw new IllegalArgumentException(
                    "Este CPF já está cadastrado.");
        }

        // 1. CRIAR USUÁRIO
        UsuariosModel usuario = new UsuariosModel();

        usuario.setEmail(email);
        usuario.setSenha(form.getSenha());
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(true);

        // O UsuariosService gera o hash BCrypt
        UsuariosModel usuarioSalvo =
                usuariosService.cadastrar(usuario);

        // 2. CRIAR CLIENTE
        ClientesModel cliente = new ClientesModel();

        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setDataNasc(form.getDataNasc());

        cliente.setLogradouro(texto(form.getLogradouro()));
        cliente.setNumero(texto(form.getNumero()));
        cliente.setComplemento(texto(form.getComplemento()));
        cliente.setBairro(texto(form.getBairro()));
        cliente.setCidade(texto(form.getCidade()));

        cliente.setEstado(
                texto(form.getEstado())
                        .toUpperCase(Locale.ROOT)
        );

        cliente.setCep(cep);

        // RELACIONAMENTO
        cliente.setUsuario(usuarioSalvo);

        clientesRepository.save(cliente);
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String somenteNumeros(String valor) {
        return texto(valor).replaceAll("\\D", "");
    }


    // CONSULTAR CLIENTE DO USUÁRIO AUTENTICADO
    public ClientesModel buscarPorUsuario(Long usuarioId) {

        return clientesRepository
                .findByUsuarioId(usuarioId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cliente não encontrado."
                        )
                );
    }

    // CARREGAR DADOS PARA O FORMULÁRIO
    public PerfilClienteForm carregarPerfil(Long usuarioId) {

        ClientesModel cliente = buscarPorUsuario(usuarioId);

        PerfilClienteForm form = new PerfilClienteForm();

        form.setNome(cliente.getNome());
        form.setCpf(cliente.getCpf());
        form.setDataNasc(cliente.getDataNasc());
        form.setEmail(cliente.getUsuario().getEmail());

        form.setLogradouro(cliente.getLogradouro());
        form.setNumero(cliente.getNumero());
        form.setComplemento(cliente.getComplemento());
        form.setBairro(cliente.getBairro());
        form.setCidade(cliente.getCidade());
        form.setEstado(cliente.getEstado());
        form.setCep(cliente.getCep());

        return form;
    }

    // ATUALIZAR DADOS CADASTRAIS
    @Transactional
    public ClientesModel atualizarPerfil(
            Long usuarioId,
            PerfilClienteForm form) {

        ClientesModel cliente = buscarPorUsuario(usuarioId);
        UsuariosModel usuario = cliente.getUsuario();

        String nome = texto(form.getNome());
        String email = texto(form.getEmail())
                .toLowerCase(Locale.ROOT);
        String cpf = somenteNumeros(form.getCpf());
        String cep = somenteNumeros(form.getCep());

        if (nome.isBlank() || nome.length() > 150) {
            throw new IllegalArgumentException(
                    "Informe um nome válido."
            );
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") ||
                email.length() > 150) {
            throw new IllegalArgumentException(
                    "Informe um e-mail válido."
            );
        }

        if (cpf.length() != 11 || cep.length() != 8) {
            throw new IllegalArgumentException(
                    "Confira o CPF e o CEP."
            );
        }

        if (form.getDataNasc() == null ||
                form.getDataNasc().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Informe uma data de nascimento válida."
            );
        }

        if (texto(form.getLogradouro()).isBlank() ||
                texto(form.getNumero()).isBlank() ||
                texto(form.getBairro()).isBlank() ||
                texto(form.getCidade()).isBlank() ||
                !texto(form.getEstado()).matches("[A-Za-z]{2}")) {

            throw new IllegalArgumentException(
                    "Preencha corretamente o endereço."
            );
        }

        // Impedir e-mail de outro usuário
        if (!usuario.getEmail().equalsIgnoreCase(email) &&
                usuariosRepository.existsByEmailIgnoreCase(email)) {

            throw new IllegalArgumentException(
                    "Este e-mail já está em uso."
            );
        }

        // Impedir CPF de outro cliente
        if (!cliente.getCpf().equals(cpf) &&
                clientesRepository.existsByCpf(cpf)) {

            throw new IllegalArgumentException(
                    "Este CPF já está cadastrado."
            );
        }

        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setDataNasc(form.getDataNasc());

        cliente.setLogradouro(texto(form.getLogradouro()));
        cliente.setNumero(texto(form.getNumero()));
        cliente.setComplemento(texto(form.getComplemento()));
        cliente.setBairro(texto(form.getBairro()));
        cliente.setCidade(texto(form.getCidade()));

        cliente.setEstado(
                texto(form.getEstado()).toUpperCase(Locale.ROOT)
        );

        cliente.setCep(cep);

        usuario.setEmail(email);

        usuariosRepository.save(usuario);

        return clientesRepository.save(cliente);
    }

}
