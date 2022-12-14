package com.kusitms.backend.repository;

import com.kusitms.backend.domain.Deal;
import com.kusitms.backend.domain.HousePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
  Boolean existsByHousePost(HousePost housePost);
}
