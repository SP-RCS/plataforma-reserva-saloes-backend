package com.ucan.reservasaloes.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
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
@Table(name = "funcionalidade")
public class Funcionalidade {

    @Id
    private Integer pkFuncionalidade;

    @Column(name = "descricao", length = 100)
    private String descricao;

    @Column(name = "designacao", length = 100)
    private String designacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_tipo_funcionalidade")
    private TipoFuncionalidade fkTipoFuncionalidade;

  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_funcionalidade_pai")
    private Funcionalidade fkFuncionalidadePai;

    @Column(name = "funcionalidades_partilhadas", length = 250)
    private String funcionalidadesPartilhadas;

    @Column(name = "url", length = 100)
    private String url;

    @Column(name = "versao", length = 100)
    private String versao;

    // ⚠️ mappedBy deve ser o nome exato do campo na entidade FuncionalidadePerfil
    @OneToMany(mappedBy = "fkFuncionalidade", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FuncionalidadePerfil> perfis = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Funcionalidade(Integer pkFuncionalidade) {
        this.pkFuncionalidade = pkFuncionalidade;
    }
}
