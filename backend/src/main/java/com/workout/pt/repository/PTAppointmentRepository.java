package com.workout.pt.repository;

import com.workout.pt.domain.contract.PTAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PTAppointmentRepository extends JpaRepository<PTAppointment, Long> {

}
