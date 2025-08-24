package com.workout.pt.repository;

import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTOffering;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PTOfferingRepository extends JpaRepository<PTOffering, Long> {

  List<PTOffering> findAllByTrainerId(Long id);
}
