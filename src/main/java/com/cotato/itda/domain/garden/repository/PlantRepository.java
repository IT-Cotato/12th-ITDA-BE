package com.cotato.itda.domain.garden.repository;

import com.cotato.itda.domain.garden.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    boolean existsByName(String name);

    @Query("SELECT p FROM Plant p ORDER BY " +
            "CASE p.difficulty WHEN com.cotato.itda.domain.garden.enums.PlantDifficulty.EASY THEN 0 " +
            "WHEN com.cotato.itda.domain.garden.enums.PlantDifficulty.NORMAL THEN 1 " +
            "ELSE 2 END ASC")
    List<Plant> findAllOrderByDifficultyAsc();

    @Query("SELECT p FROM Plant p ORDER BY " +
            "CASE p.difficulty WHEN com.cotato.itda.domain.garden.enums.PlantDifficulty.EASY THEN 0 " +
            "WHEN com.cotato.itda.domain.garden.enums.PlantDifficulty.NORMAL THEN 1 " +
            "ELSE 2 END DESC")
    List<Plant> findAllOrderByDifficultyDesc();

    Optional<Plant> findByName(String name);
}
