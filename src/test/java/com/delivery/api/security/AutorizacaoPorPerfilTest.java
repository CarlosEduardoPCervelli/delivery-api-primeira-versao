package com.delivery.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante que as regras @PreAuthorize(hasRole(...)) estão realmente ativas
 * (dependem de @EnableMethodSecurity no SecurityConfig).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AutorizacaoPorPerfilTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNaoPodeListarEntregasDisponiveis() throws Exception {
        mockMvc.perform(get("/pedidos/entregas/disponiveis"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNaoPodeAceitarEntrega() throws Exception {
        mockMvc.perform(patch("/pedidos/1/aceitar-entrega"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ENTREGADOR")
    void entregadorNaoPodeAtualizarStatusComoRestaurante() throws Exception {
        mockMvc.perform(patch("/pedidos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_PREPARO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ENTREGADOR")
    void entregadorPodeListarEntregasDisponiveis() throws Exception {
        mockMvc.perform(get("/pedidos/entregas/disponiveis"))
                .andExpect(status().isOk());
    }
}
