package com.ucan.reservasaloes.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pessoa")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_pessoa")
    private Integer pkPessoa;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nome;

    @NotNull
    @Past
    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = true, length = 50)
    private String identificacao;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_genero")
    private Genero fkGenero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_estado_civil")
    private EstadoCivil fkEstadoCivil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_localidade")
    private Localidade localidade;

    @OneToMany(mappedBy = "fkPessoa", fetch = FetchType.LAZY)
    private List<Conta> contas;
    
    @OneToMany(mappedBy = "fkPessoa", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Telefone> telefones = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pessoa)) return false;
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(pkPessoa, pessoa.pkPessoa) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkPessoa);
    }
}
