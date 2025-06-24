package com.part4.team09.otboo.module.domain.weather.repository;

import com.part4.team09.otboo.module.domain.weather.entity.Temperature;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemperatureRepository extends JpaRepository<Temperature, UUID> {

}
