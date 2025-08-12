package com.workout.global.version;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterDataVersionRepository extends JpaRepository<MasterDataVersion, String> {
}