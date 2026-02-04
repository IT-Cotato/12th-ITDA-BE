package com.cotato.itda.domain.garden.entity;

import com.cotato.itda.domain.garden.enums.PlantStage;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "shared_plants")
public class SharedPlant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_a_id", nullable = false)
    private Member memberA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_b_id", nullable = false)
    private Member memberB;

    @Column(name = "nickname", nullable = false, length = 20)
    private String nickname;

    @Column(name = "growth_value", nullable = false)
    @Builder.Default
    private int growthValue = 0;

    @Column(name = "growth_stage", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PlantStage growthStage = PlantStage.SEED;

    @Column(name = "daily_growth_count", nullable = false)
    @Builder.Default
    private int dailyGrowthCount = 0;

    @Column(name = "growth_date")
    private LocalDate growthDate;

    @Column(name = "last_watered_by")
    private Long lastWateredBy;

    @Column(name = "is_solo_mode", nullable = false)
    @Builder.Default
    private Boolean isSoloMode = false;

    @Column(name = "solo_power_member_id")
    private Long soloPowerMemberId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SharedPlantStatus status = SharedPlantStatus.GROWING;

    public void water(int growthValue, Member member, boolean isFirst) {
        this.growthValue += growthValue;
        this.growthStage = this.plant.calculateGrowthStage(this.growthValue);
        if (isFirst) this.dailyGrowthCount = 1;
        else this.dailyGrowthCount++;
        this.lastWateredBy = member.getId();
        this.growthDate = LocalDate.now();
    }

    public void nutrient(int growthValue, Member member) {
        this.growthValue += growthValue;
        this.growthStage = this.plant.calculateGrowthStage(this.growthValue);
        member.decreaseNutrient();
    }

    public void exitSoloMode() {
        this.isSoloMode = false;
        this.soloPowerMemberId = null;
    }

    public void complete() {
        this.status = SharedPlantStatus.COMPLETED;
    }

    public void revive() {
        this.status = SharedPlantStatus.GROWING;
    }

    public boolean hasReachedMaxGrowth() {
        return this.growthValue >= this.plant.getBloomMax();
    }
}
