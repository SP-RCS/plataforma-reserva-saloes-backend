package com.ucan.reservasaloes.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Sebastiao
 */
@Getter
@Setter
@Entity

@Table(name = "genero")
public class Genero {

    @Id
    @GeneratedValue
    @Column(name = "pk_genero")
    private Integer pkGenero;

    @NotBlank(message = "O nome não pode estar em branco")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    public Genero() {
    }

    
    public Genero(Integer pkGenero) {
        this.pkGenero = pkGenero;
    }

    public Genero(Integer pkGenero, String nome) {
        this.pkGenero = pkGenero;
        this.nome = nome;
    }
    
    



}
