package com.cotato.itda.domain.mission.entity;

import com.cotato.itda.domain.mission.enums.MissionCategory;
import com.cotato.itda.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Table(name = "mission")
public class Mission extends BaseEntity {

    @Column(name = "mission_date", nullable = false)
    private LocalDate missionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private MissionCategory category;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "content", nullable = false)
    private String content;

}
