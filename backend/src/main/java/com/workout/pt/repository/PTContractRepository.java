package com.workout.pt.repository;

import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PTContractRepository extends JpaRepository<PTContract, Long> {

  List<PTContract> findAllByTrainerId(Long memberId);

  List<PTContract> findByTrainerIdAndStatus(Long trainerId, PTContractStatus status);

}
