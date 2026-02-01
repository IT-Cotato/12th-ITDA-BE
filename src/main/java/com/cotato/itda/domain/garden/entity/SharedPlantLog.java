package com.cotato.itda.domain.garden.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shared_plant_log")
public class SharedPlantLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_plant_id", nullable = false)
    private SharedPlant sharedPlant;

    @Column(name = "watered_by", nullable = false)
    private Long wateredBy;

    @Column(name = "watered_at", nullable = false)
    @Builder.Default
    private LocalDateTime wateredAt = LocalDateTime.now();

    @Column(name = "affected_growth", nullable = false)
    private boolean affectedGrowth;

    @Column(name = "growth_increment", nullable = false)
    @Builder.Default
    private int growthIncrement = 5;

    @Column(name = "used_nutrient", nullable = false)
    @Builder.Default
    private boolean usedNutrient = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
