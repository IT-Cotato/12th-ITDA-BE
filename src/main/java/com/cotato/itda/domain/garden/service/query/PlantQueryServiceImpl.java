package com.cotato.itda.domain.garden.service.query;

import com.cotato.itda.domain.garden.converter.PlantConverter;
import com.cotato.itda.domain.garden.dto.res.PlantResDTO;
import com.cotato.itda.domain.garden.entity.Plant;
import com.cotato.itda.domain.garden.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantQueryServiceImpl implements PlantQueryService {

    private final PlantRepository plantRepository;

    @Override
    public PlantResDTO.PlantListResDto getPlantList(Sort sort) {
        List<Plant> plants;

        if (sort != null && sort.isSorted()) {
            Sort.Order difficultyOrder = sort.getOrderFor("difficulty");
            if (difficultyOrder != null) {
                if (difficultyOrder.isAscending()) {
                    plants = plantRepository.findAllOrderByDifficultyAsc();
                } else {
                    plants = plantRepository.findAllOrderByDifficultyDesc();
                }
            } else {
                plants = plantRepository.findAll(sort);
            }
        } else {
            plants = plantRepository.findAll(Sort.unsorted());
        }

        return PlantConverter.toPlantListResDto(plants);
    }
}
