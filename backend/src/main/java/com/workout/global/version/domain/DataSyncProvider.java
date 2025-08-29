package com.workout.global.version.domain;

import java.util.List;

public interface DataSyncProvider {
  MasterDataCategory getCategory();
  List<?> getSyncData();
}