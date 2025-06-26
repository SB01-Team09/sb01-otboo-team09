package com.part4.team09.otboo.module.domain.location.batch;

import com.part4.team09.otboo.module.domain.location.dto.response.TLocation;
import com.part4.team09.otboo.module.domain.location.entity.Dong;
import com.part4.team09.otboo.module.domain.location.entity.Gu;
import com.part4.team09.otboo.module.domain.location.entity.Location;
import com.part4.team09.otboo.module.domain.location.entity.Sido;
import com.part4.team09.otboo.module.domain.location.repository.DongRepository;
import com.part4.team09.otboo.module.domain.location.repository.GuRepository;
import com.part4.team09.otboo.module.domain.location.repository.SidoRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TLocationProcessor implements ItemProcessor<TLocation, Location> {

  private final SidoRepository sidoRepository;
  private final GuRepository guRepository;
  private final DongRepository dongRepository;

  @Override
  public Location process(TLocation item) throws Exception {

    if (item.level2() == null || item.level2().isBlank()) {
      return null;  // null 반환하면 해당 아이템은 다음 단계로 넘어가지 않고 무시됨
    }

    if (item.level3() == null || item.level3().isBlank()) {
      return null;
    }

    String id = item.category();

    String sidoName = item.level1();
    Sido sido = sidoRepository.findBySidoName(sidoName)
      .orElseGet(() -> sidoRepository.save(Sido.create(sidoName)));

    String guName = item.level2();
    Gu gu = guRepository.findByGuName(guName)
      .orElseGet(() -> guRepository.save(Gu.create(guName)));

    String dongName = item.level3();
    int x = item.gridX();
    int y = item.gridY();
    double longitude = item.longitude();
    double latitude = item.latitude();
    Dong dong = dongRepository.findByDongName(dongName)
      .orElseGet(() -> dongRepository.save(
        Dong.create(dongName, latitude, longitude, x, y)
      ));


    return Location.create(
      id,
      sido.getId(),
      gu.getId(),
      dong.getId()
    );
  }
}

