package com.ucan.reservasaloes.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "funcionalidade_perfil")
public class FuncionalidadePerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pk_funcionalidade_perfil")
    private Integer pkFuncionalidadePerfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_perfil", nullable = false)
    private Perfil fkPerfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_funcionalidade", nullable = false)
    private Funcionalidade fkFuncionalidade;

    @Column(name = "detalhe", length = 500)
    private String detalhe;
    
    
    @Column(name = "is_herdada_de_perfil")
    private Boolean isHerdadaDePerfil = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FuncionalidadePerfil(Funcionalidade fkFuncionalidade, Perfil fkPerfil, String detalhe, LocalDateTime createdAt) {
        this.fkFuncionalidade = fkFuncionalidade;
        this.fkPerfil = fkPerfil;
        this.detalhe = detalhe;
        this.createdAt = createdAt;
    }
}
