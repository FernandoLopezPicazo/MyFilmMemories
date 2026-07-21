package com.fernando.seriestracker.service;

import com.fernando.seriestracker.config.UsuarioActualService;
import com.fernando.seriestracker.entity.Perfil;
import com.fernando.seriestracker.repository.PerfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final UsuarioActualService usuarioActual;

    @Transactional
    public void sincronizar() {
        UUID id = usuarioActual.obtenerId();
        String email = usuarioActual.obtenerEmail();

        Perfil perfil = perfilRepository.findById(id).orElse(new Perfil(id, email));
        if (!perfil.getEmail().equalsIgnoreCase(email)) {
            perfil.setEmail(email);
        }
        perfilRepository.save(perfil);
    }

    @Transactional(readOnly = true)
    public Optional<Perfil> buscarPorEmail(String email) {
        return perfilRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public Optional<Perfil> buscarPorId(UUID usuarioId) {
        return perfilRepository.findById(usuarioId);
    }
}
