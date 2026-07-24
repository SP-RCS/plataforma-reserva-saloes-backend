package com.ucan.reservasaloes.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pkPerfil;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String designacao;

    @Size(max = 200)
    private String descricao;

    @Column(name = "estado")
    private Integer estado;

    // ⚠️ mappedBy deve ser o nome exato do campo na entidade FuncionalidadePerfil
    @OneToMany(mappedBy = "fkPerfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FuncionalidadePerfil> funcionalidades = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Perfil)) return false;
        Perfil perfil = (Perfil) o;
        return Objects.equals(pkPerfil, perfil.pkPerfil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkPerfil);
    }

    public Perfil(Integer pkPerfil) {
        this.pkPerfil = pkPerfil;
    }
}
