package com.delivery.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "entregadores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entregador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "usuario_id", unique = true, nullable = false)
    private Usuario usuario;

    @NotBlank
    private String veiculo;

    @Builder.Default
    private boolean disponivel = true;
}
