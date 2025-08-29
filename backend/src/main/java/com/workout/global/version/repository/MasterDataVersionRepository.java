package com.workout.global.version.repository;

import com.workout.global.version.domain.MasterDataVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterDataVersionRepository extends JpaRepository<MasterDataVersion, String> {

}