package com.workout.global.version.service;

import com.workout.global.version.domain.DataSyncProvider;
import com.workout.global.version.domain.MasterDataCategory;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DataSyncService {

  private final List<DataSyncProvider> providers;

  private final Map<MasterDataCategory, DataSyncProvider> providerMap = new EnumMap<>(MasterDataCategory.class);

  public DataSyncService(List<DataSyncProvider> providers) {
    this.providers = providers;
  }

  @PostConstruct
  public void initialize() {
    for (DataSyncProvider provider : providers) {
      providerMap.put(provider.getCategory(), provider);
    }
  }

  public List<?> getSyncDataFor(MasterDataCategory category) {
    DataSyncProvider provider = providerMap.get(category);
    if (provider == null) {
      throw new IllegalArgumentException("Unsupported data category: " + category.name());
    }
    return provider.getSyncData();
  }
}