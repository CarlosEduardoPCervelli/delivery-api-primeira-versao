package com.delivery.api.dto.auth;

import com.delivery.api.model.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres") String senha,
        @NotNull Perfil perfil,

        // Campos usados conforme o perfil
        String telefone,               // CLIENTE
        String rua,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String cep,

        String nomeFantasia,           // RESTAURANTE
        String categoria,

        String veiculo                 // ENTREGADOR
) {}
