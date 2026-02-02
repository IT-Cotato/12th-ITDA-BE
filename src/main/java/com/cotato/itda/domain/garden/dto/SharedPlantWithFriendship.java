package com.cotato.itda.domain.garden.dto;

import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.garden.entity.SharedPlant;

public record SharedPlantWithFriendship(
        SharedPlant sharedPlant,
        Friendship friendship
) {}
