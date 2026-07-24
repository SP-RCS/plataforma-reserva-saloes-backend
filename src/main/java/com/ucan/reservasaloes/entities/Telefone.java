package com.ucan.reservasaloes.entities;

import com.ucan.reservasaloes.enumerable.TipoTelefoneEnum; // ADICIONE ESTE IMPORT
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "telefone")
public class Telefone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_telefone")
    private Integer pkTelefone;

    @NotBlank(message = "O número de telefone não pode estar em branco")
    @Size(min = 8, max = 20, message = "O telefone deve ter entre 8 e 20 caracteres")
    @Pattern(regexp = "^[+]?[0-9\\s-]+$", message = "Número de telefone inválido")
    @Column(name = "numero", nullable = false, length = 20, unique = true)
    private String numero;

    @NotNull(message = "A pessoa associada não pode ser nula")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_pessoa", referencedColumnName = "pk_pessoa", nullable = false)
    private Pessoa fkPessoa;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 15)
    private TipoTelefoneEnum tipo;  // ADICIONE ESTE CAMPO - CORREÇÃO AQUI

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Telefone that = (Telefone) o;
        return Objects.equals(pkTelefone, that.pkTelefone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkTelefone);
    }

    @Override
    public String toString() {
        return "Telefone{" +
                "id=" + pkTelefone +
                ", numero='" + numero + '\'' +
                ", pessoaId=" + (fkPessoa != null ? fkPessoa.hashCode() : "null") +
                ", tipo=" + tipo +
                '}';
    }
}
