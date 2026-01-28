package com.cotato.itda.domain.garden.entity;

import com.cotato.itda.domain.garden.enums.PlantDifficulty;
import com.cotato.itda.domain.garden.enums.PlantStage;
import com.cotato.itda.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "plants", uniqueConstraints = @UniqueConstraint(columnNames = {"name"}))
public class Plant extends BaseEntity {

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "difficulty", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlantDifficulty difficulty;

    @Column(name = "duration", nullable = false)
    private int duration;

    @Column(name = "hashtags", nullable = false)
    private String hashtags;

    @Column(name = "height", nullable = false)
    private int height;

    @Column(name = "seed_max", nullable = false)
    private int seedMax;

    @Column(name = "sprout_max", nullable = false)
    private int sproutMax;

    @Column(name = "stem_max", nullable = false)
    private int stemMax;

    @Column(name = "bud_max", nullable = false)
    private int budMax;

    @Column(name = "bloom_max", nullable = false)
    private int bloomMax;

    public PlantStage calculateGrowthStage(int growthValue) {
        if (growthValue <= seedMax) return PlantStage.SEED;
        else if (growthValue <= sproutMax) return PlantStage.SPROUT;
        else if (growthValue <= stemMax) return PlantStage.STEM;
        else if (growthValue <= budMax) return PlantStage.BUD;
        else return PlantStage.BLOOM;
    }
}
