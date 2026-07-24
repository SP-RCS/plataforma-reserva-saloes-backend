package com.ucan.reservasaloes.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.ucan.reservasaloes.enumerable.TipoLocalidade;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "localidades")
@Getter
@Setter
public class Localidade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "pk_localidade")
    private Integer pkLocalidade;

    @Basic(optional = false)
    @Column(name = "nome", nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoLocalidade tipo;

    @Column(name = "nome_rua_ou_numero_rua")
    private String nomeRua;

    @JsonManagedReference
    @OneToMany(mappedBy = "localidadePai", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Localidade> filhas;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_localidade_pai")
    private Localidade localidadePai;

    public Localidade() { }

    public Localidade(Integer pkLocalidade) {
        this.pkLocalidade = pkLocalidade;
    }

    public Localidade(Integer pkLocalidade, String nome, TipoLocalidade tipo) {
        this.pkLocalidade = pkLocalidade;
        this.nome = nome;
        this.tipo = tipo;
    }

    public Localidade(String nome, TipoLocalidade tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Localidade{" +
                "pkLocalidade=" + pkLocalidade +
                ", nome='" + nome + '\'' +
                ", tipo=" + tipo +
                ", nomeRua='" + nomeRua + '\'' +
                '}';
    }
}
