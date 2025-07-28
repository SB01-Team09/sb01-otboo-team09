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

  @Test
  void deleteAllByFeedId() {
    // given
    UUID feedId1 = UUID.randomUUID();
    UUID feedId2 = UUID.randomUUID();
    UUID clothesId = UUID.randomUUID();

    entityManager.persist(Ootd.create(feedId1, clothesId));
    entityManager.persist(Ootd.create(feedId2, clothesId));
    entityManager.flush();

    // when
    ootdRepository.deleteAllByFeedId(feedId1);
    entityManager.flush();
    entityManager.clear();

    // then
    List<Ootd> remaining = ootdRepository.findAll();
    assertThat(remaining).hasSize(1);
    assertThat(remaining.get(0).getFeedId()).isEqualTo(feedId2);
  }

  @Test
  void deleteAllByClothesId() {
    // given
    UUID feedId = UUID.randomUUID();
    UUID clothesId1 = UUID.randomUUID();
    UUID clothesId2 = UUID.randomUUID();

    entityManager.persist(Ootd.create(feedId, clothesId1));
    entityManager.persist(Ootd.create(feedId, clothesId2));
    entityManager.flush();

    // when
    ootdRepository.deleteByClothesId(clothesId1);
    entityManager.flush();
    entityManager.clear();

    // then
    List<Ootd> remaining = ootdRepository.findAll();
    assertThat(remaining).hasSize(1);
    assertThat(remaining.get(0).getClothesId()).isEqualTo(clothesId2);
  }
}