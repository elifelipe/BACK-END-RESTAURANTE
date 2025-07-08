// src/main/java/br/com/elftech/elftech/dto/MarketingImageResponse.java
package br.com.elftech.elftech.dto;

import br.com.elftech.elftech.model.MarketingImage;
import java.util.UUID;

public record MarketingImageResponse(UUID id, String imageUrl) {
    public MarketingImageResponse(MarketingImage marketingImage) {
        this(marketingImage.getId(), marketingImage.getImageUrl());
    }
}
