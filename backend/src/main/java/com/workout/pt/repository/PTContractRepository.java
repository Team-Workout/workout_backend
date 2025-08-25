package com.workout.pt.repository;

import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PTContractRepository extends JpaRepository<PTContract, Long> {

  Page<PTContract> findAllByMemberId(Long memberId, Pageable pageable);

  Page<PTContract> findAllByTrainerId(Long trainerId, Pageable pageable);

  Page<PTContract> findByTrainerIdAndStatus(Long trainerId, PTContractStatus status, Pageable pageable);

  boolean existsByTrainerIdAndMemberIdAndStatus(Long trainerId, Long memberId, PTContractStatus status);}
