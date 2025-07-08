// src/main/java/br/com/elftech/elftech/model/MarketingImage.java
package br.com.elftech.elftech.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "marketing_images")
public class MarketingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 1024) // Increased length for URLs
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    public MarketingImage(String imageUrl, Restaurante restaurante) {
        this.imageUrl = imageUrl;
        this.restaurante = restaurante;
    }
}
