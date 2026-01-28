package com.cotato.itda.domain.garden.repository;

import com.cotato.itda.domain.garden.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlantRepository extends JpaRepository<Plant, Long> {
}
