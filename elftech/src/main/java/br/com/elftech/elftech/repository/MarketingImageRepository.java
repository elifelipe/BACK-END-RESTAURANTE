// src/main/java/br/com/elftech/elftech/repository/MarketingImageRepository.java
package br.com.elftech.elftech.repository;

import br.com.elftech.elftech.model.MarketingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketingImageRepository extends JpaRepository<MarketingImage, UUID> {
    List<MarketingImage> findByRestauranteId(UUID restauranteId);
    Optional<MarketingImage> findByRestauranteIdAndImageUrl(UUID restauranteId, String imageUrl);
    void deleteByRestauranteIdAndImageUrl(UUID restauranteId, String imageUrl);
}
