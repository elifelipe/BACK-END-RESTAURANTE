// src/main/java/br/com/elftech/elftech/controller/MarketingImageController.java
package br.com.elftech.elftech.controller;

import br.com.elftech.elftech.service.MarketingImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MarketingImageController {

    @Autowired
    private MarketingImageService marketingImageService;

    // Public endpoint to get marketing images (for client-side menu)
    @GetMapping("/public/restaurantes/{restauranteId}/marketing-images")
    public ResponseEntity<List<String>> getPublicMarketingImages(@PathVariable UUID restauranteId) {
        List<String> imageUrls = marketingImageService.getMarketingImages(restauranteId);
        return ResponseEntity.ok(imageUrls);
    }

    // Authenticated endpoint to get marketing images (for management)
    @GetMapping("/restaurantes/{restauranteId}/marketing-images")
    public ResponseEntity<List<String>> getMarketingImages(@PathVariable UUID restauranteId) {
        List<String> imageUrls = marketingImageService.getMarketingImages(restauranteId);
        return ResponseEntity.ok(imageUrls);
    }

    // Authenticated endpoint to add a marketing image
    @PostMapping("/restaurantes/{restauranteId}/marketing-images")
    public ResponseEntity<List<String>> addMarketingImage(
            @PathVariable UUID restauranteId,
            @RequestBody Map<String, String> requestBody) {
        String imageUrl = requestBody.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        List<String> updatedImageUrls = marketingImageService.addMarketingImage(restauranteId, imageUrl);
        return ResponseEntity.status(201).body(updatedImageUrls);
    }

    // Authenticated endpoint to delete a marketing image
    @DeleteMapping("/restaurantes/{restauranteId}/marketing-images")
    public ResponseEntity<Void> deleteMarketingImage(
            @PathVariable UUID restauranteId,
            @RequestBody Map<String, String> requestBody) {
        String imageUrl = requestBody.get("imageUrl");
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        marketingImageService.deleteMarketingImage(restauranteId, imageUrl);
        return ResponseEntity.noContent().build();
    }
}
