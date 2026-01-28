package com.cotato.itda.domain.garden.repository;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedPlantRepository extends JpaRepository<SharedPlant, Long> {
}
