
package senai499.com.br.salaotesouraeprosa.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "funcionarios")
public class FuncionariosModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @OneToOne(optional = false)
    @JoinColumn(
            name = "usuario_id",
            nullable = false,
            unique = true)
    private UsuariosModel usuario;

    @ManyToMany
    @JoinTable(
            name = "funcionarios_servicos",
            joinColumns = @JoinColumn(
                    name = "funcionario_id"),
            inverseJoinColumns = @JoinColumn(
                    name = "servico_id")
    )
    private Set<ServicosModel> servicos = new HashSet<>();

    public FuncionariosModel() {}

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public UsuariosModel getUsuario() {
        return usuario;
    }
    public void setUsuario(UsuariosModel usuario) {
        this.usuario = usuario;
    }

    public Set<ServicosModel> getServicos() {
        return servicos;
    }

    public void setServicos(Set<ServicosModel> servicos) {
        this.servicos = servicos;
    }
}
