package com.ucan.reservasaloes.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "conta_perfil")
public class ContaPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_conta_perfil")
    private Integer pkContaPerfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_conta")
    private Conta fkConta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_perfil")
    private Perfil fkPerfil;

    @Column(nullable = false)
    private int estado = 1;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContaPerfil)) return false;
        ContaPerfil that = (ContaPerfil) o;
        return Objects.equals(pkContaPerfil, that.pkContaPerfil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkContaPerfil);
    }
}
