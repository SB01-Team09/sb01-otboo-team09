package com.part4.team09.otboo.module.domain.location.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "locations",
    uniqueConstraints = {
      @UniqueConstraint(
        name = "UK_SidoGuDong",
        columnNames = {"sidoId", "guId", "dongId"})
    }
    )
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Location {

  @Id
  @Column(length = 10)
  private String id;

  @Column(nullable = false)
  private UUID sidoId;

  @Column(nullable = false)
  private UUID guId;

  @Column(nullable = false)
  private UUID dongId;

  @CreatedDate
  @Column(columnDefinition = "timestamp with time zone", updatable = false, nullable = false)
  private LocalDateTime createdAt;

  public static Location create(String id, UUID sidoId, UUID guId, UUID dongId) {
    return new Location(id, sidoId, guId, dongId);
  }

  private Location(String id, UUID sidoId, UUID guId, UUID dongId) {
    this.id = id;
    this.sidoId = sidoId;
    this.guId = guId;
    this.dongId = dongId;
  }
}
