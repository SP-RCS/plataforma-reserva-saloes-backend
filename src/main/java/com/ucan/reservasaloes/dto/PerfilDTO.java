package com.ucan.reservasaloes.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class PerfilDTO {

    private Integer pkPerfil;
    private String designacao;
    private String descricao;
    private Integer estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> funcionalidades; 
     private List<Integer> herdarDePerfis;
}
