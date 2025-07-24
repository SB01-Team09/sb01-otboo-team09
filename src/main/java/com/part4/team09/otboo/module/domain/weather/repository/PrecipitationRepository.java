package com.part4.team09.otboo.module.domain.weather.repository;

import com.part4.team09.otboo.module.domain.weather.entity.Precipitation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrecipitationRepository extends JpaRepository<Precipitation, UUID> {

}
