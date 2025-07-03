package br.com.elftech.elftech.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Where;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurantes")
@Where(clause = "ativo = true")
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // --- CAMPOS QUE FALTAVAM ADICIONADOS AQUI ---
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @Column(nullable = false)
    private boolean ativo = true;
    // --- FIM DOS CAMPOS QUE FALTAVAM ---

    @OneToMany(mappedBy = "restaurante", fetch = FetchType.LAZY)
    private List<Usuario> usuarios;

    // Seus Getters e Setters manuais agora funcionarão
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public List<Usuario> getUsuarios() { return usuarios; }
    public void setUsuarios(List<Usuario> usuarios) { this.usuarios = usuarios; }
}