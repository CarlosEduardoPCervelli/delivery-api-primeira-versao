package com.delivery.api.service;

import com.delivery.api.dto.restaurante.RestauranteResponse;
import com.delivery.api.dto.restaurante.RestauranteUpdateRequest;
import com.delivery.api.exception.ResourceNotFoundException;
import com.delivery.api.model.Restaurante;
import com.delivery.api.repository.RestauranteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;

    public Page<RestauranteResponse> listar(String categoria, String nome, Pageable pageable) {
        Page<Restaurante> pagina;
        if (categoria != null && !categoria.isBlank()) {
            pagina = restauranteRepository.findByCategoriaIgnoreCaseAndAtivoTrue(categoria, pageable);
        } else if (nome != null && !nome.isBlank()) {
            pagina = restauranteRepository.findByNomeFantasiaContainingIgnoreCaseAndAtivoTrue(nome, pageable);
        } else {
            pagina = restauranteRepository.findByAtivoTrue(pageable);
        }
        return pagina.map(RestauranteResponse::from);
    }

    public RestauranteResponse buscarPorId(Long id) {
        return RestauranteResponse.from(buscarEntidadePorId(id));
    }

    public Restaurante buscarEntidadePorId(Long id) {
        return restauranteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado: " + id));
    }

    public Restaurante buscarEntidadePorEmail(String email) {
        return restauranteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurante não encontrado para o usuário logado"));
    }

    @Transactional
    public RestauranteResponse atualizarPerfil(String emailUsuarioLogado, RestauranteUpdateRequest req) {
        Restaurante restaurante = buscarEntidadePorEmail(emailUsuarioLogado);

        if (req.nomeFantasia() != null) restaurante.setNomeFantasia(req.nomeFantasia());
        if (req.categoria() != null) restaurante.setCategoria(req.categoria());
        if (req.horaAbertura() != null) restaurante.setHoraAbertura(req.horaAbertura());
        if (req.horaFechamento() != null) restaurante.setHoraFechamento(req.horaFechamento());
        if (req.ativo() != null) restaurante.setAtivo(req.ativo());

        return RestauranteResponse.from(restauranteRepository.save(restaurante));
    }
}
