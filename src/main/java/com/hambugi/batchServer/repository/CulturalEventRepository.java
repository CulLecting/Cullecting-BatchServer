package com.hambugi.batchServer.repository;

import com.hambugi.batchServer.entity.CulturalEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CulturalEventRepository extends JpaRepository<CulturalEvent, Object> {

}
