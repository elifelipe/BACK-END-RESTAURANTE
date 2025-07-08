// src/main/java/br/com/elftech/elftech/service/MarketingImageService.java
package br.com.elftech.elftech.service;

import br.com.elftech.elftech.model.MarketingImage;
import br.com.elftech.elftech.model.Restaurante;
import br.com.elftech.elftech.model.Usuario;
import br.com.elftech.elftech.repository.MarketingImageRepository;
import br.com.elftech.elftech.repository.RestauranteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MarketingImageService {

    @Autowired
    private MarketingImageRepository marketingImageRepository;

    @Autowired
    private RestauranteRepository restauranteRepository;

    @Transactional
    public List<String> addMarketingImage(UUID restauranteId, String imageUrl) {
        validarAcessoRestaurante(restauranteId);
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado com ID: " + restauranteId));

        // Check if the image URL already exists for this restaurant
        if (marketingImageRepository.findByRestauranteIdAndImageUrl(restauranteId, imageUrl).isPresent()) {
            throw new IllegalStateException("Esta URL de imagem já existe para este restaurante.");
        }

        MarketingImage marketingImage = new MarketingImage(imageUrl, restaurante);
        marketingImageRepository.save(marketingImage);

        // Return the updated list of image URLs
        return marketingImageRepository.findByRestauranteId(restauranteId)
                .stream()
                .map(MarketingImage::getImageUrl)
                .collect(Collectors.toList());
    }

    public List<String> getMarketingImages(UUID restauranteId) {
        // No authentication check needed for public access, but still validate if restaurant exists
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado com ID: " + restauranteId));

        return marketingImageRepository.findByRestauranteId(restauranteId)
                .stream()
                .map(MarketingImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMarketingImage(UUID restauranteId, String imageUrl) {
        validarAcessoRestaurante(restauranteId);
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurante não encontrado com ID: " + restauranteId));

        MarketingImage marketingImage = marketingImageRepository.findByRestauranteIdAndImageUrl(restauranteId, imageUrl)
                .orElseThrow(() -> new EntityNotFoundException("Imagem de marketing não encontrada para esta URL e restaurante."));

        marketingImageRepository.delete(marketingImage);
    }

    private void validarAcessoRestaurante(UUID restauranteIdUrl) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("Acesso negado: usuário não autenticado.");
        }
        if (!(authentication.getPrincipal() instanceof Usuario)) {
            throw new SecurityException("Tipo de principal inesperado. Não é um objeto Usuario.");
        }
        Usuario usuarioLogado = (Usuario) authentication.getPrincipal();
        if (usuarioLogado.getRestaurante() == null) {
            throw new SecurityException("Acesso negado: usuário sem restaurante associado.");
        }
        if (!usuarioLogado.getRestaurante().getId().equals(restauranteIdUrl)) {
            throw new SecurityException("Acesso negado: o usuário não tem permissão para acessar este restaurante.");
        }
    }
}
