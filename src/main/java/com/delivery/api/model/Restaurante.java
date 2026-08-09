package com.delivery.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    @NotBlank
    private String nomeFantasia;

    @NotBlank
    private String categoria;

    @Embedded
    private Endereco endereco;

    private LocalTime horaAbertura;

    private LocalTime horaFechamento;

    @Builder.Default
    private boolean ativo = true;

    @Builder.Default
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produto> produtos = new ArrayList<>();

    @Transient
    public boolean estaAberto() {
        if (!ativo || horaAbertura == null || horaFechamento == null) {
            return ativo;
        }
        LocalTime agora = LocalTime.now();
        return !agora.isBefore(horaAbertura) && !agora.isAfter(horaFechamento);
    }
}
