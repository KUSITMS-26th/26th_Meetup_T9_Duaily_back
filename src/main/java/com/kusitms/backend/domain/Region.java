package com.kusitms.backend.domain;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class Region extends MetaEntity {

  @Id
  @GeneratedValue
  @Column(name = "region_id")
  private Long id;
  private String name; // 지역명
  private String content; // 간단 추천 이유
  private String origin; // 지역 유래
  private String info; // 지역 정보

  @Embedded
  private Address address; // 지역 위치

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "region_id")
  private Set<ImageFile> imageFiles;

}