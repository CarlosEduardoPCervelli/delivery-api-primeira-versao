package com.delivery.api.service;

import com.delivery.api.dto.auth.AuthResponse;
import com.delivery.api.dto.auth.LoginRequest;
import com.delivery.api.dto.auth.RegisterRequest;
import com.delivery.api.exception.BusinessException;
import com.delivery.api.model.*;
import com.delivery.api.model.enums.Perfil;
import com.delivery.api.repository.ClienteRepository;
import com.delivery.api.repository.EntregadorRepository;
import com.delivery.api.repository.RestauranteRepository;
import com.delivery.api.repository.UsuarioRepository;
import com.delivery.api.security.CustomUserDetails;
import com.delivery.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RestauranteRepository restauranteRepository;
    private final EntregadorRepository entregadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new BusinessException("Já existe um usuário cadastrado com este e-mail");
        }

        Usuario usuario = Usuario.builder()
                .nome(req.nome())
                .email(req.email())
                .senha(passwordEncoder.encode(req.senha()))
                .perfil(req.perfil())
                .build();

        switch (req.perfil()) {
            case CLIENTE -> {
                Endereco endereco = new Endereco(req.rua(), req.numero(), req.complemento(), req.bairro(), req.cidade(), req.cep());
                Cliente cliente = Cliente.builder()
                        .usuario(usuario)
                        .telefone(req.telefone())
                        .endereco(endereco)
                        .build();
                clienteRepository.save(cliente);
            }
            case RESTAURANTE -> {
                Endereco endereco = new Endereco(req.rua(), req.numero(), req.complemento(), req.bairro(), req.cidade(), req.cep());
                Restaurante restaurante = Restaurante.builder()
                        .usuario(usuario)
                        .nomeFantasia(req.nomeFantasia())
                        .categoria(req.categoria())
                        .endereco(endereco)
                        .build();
                restauranteRepository.save(restaurante);
            }
            case ENTREGADOR -> {
                Entregador entregador = Entregador.builder()
                        .usuario(usuario)
                        .veiculo(req.veiculo())
                        .build();
                entregadorRepository.save(entregador);
            }
        }

        String token = jwtService.generateToken(new CustomUserDetails(usuario));
        return new AuthResponse(token, usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name());
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.senha()));

        Usuario usuario = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        String token = jwtService.generateToken(new CustomUserDetails(usuario));
        return new AuthResponse(token, usuario.getNome(), usuario.getEmail(), usuario.getPerfil().name());
    }
}
