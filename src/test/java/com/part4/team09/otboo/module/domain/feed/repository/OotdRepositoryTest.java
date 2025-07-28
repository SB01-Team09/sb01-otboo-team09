package com.part4.team09.otboo.module.domain.feed.repository;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.part4.team09.otboo.module.domain.feed.entity.Ootd;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@EnableJpaAuditing
@ActiveProfiles("test")
@EnableJpaRepositories(basePackageClasses = OotdRepository.class)
class OotdRepositoryTest {

  @Autowired
  private OotdRepository ootdRepository;

  @Autowired
  private EntityManager entityManager;

  @Test
  void findClothesIdsByFeedId() {
    // given
    UUID feedId = UUID.randomUUID();
    UUID clothesId = UUID.randomUUID();

    Ootd ootd = Ootd.create(feedId, clothesId);
    entityManager.persist(ootd);
    entityManager.flush();

    // when
    List<UUID> result = ootdRepository.findClothesIdsByFeedId(feedId);

    // then
    assertThat(result).containsExactly(clothesId);
  }
}