package com.workout.global.version.service;

import com.workout.global.version.domain.DataVersion;
import com.workout.global.version.domain.MasterDataCategory;
import com.workout.global.version.domain.MasterDataVersion;
import com.workout.global.version.repository.MasterDataVersionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MasterDataVersionService {

  private final MasterDataVersionRepository versionRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateVersion(MasterDataCategory category) {
    String categoryName = category.name();
    MasterDataVersion version = versionRepository.findById(categoryName)
        .orElse(new MasterDataVersion(categoryName, 0L));

    version.updateVersion(version.getVersion() + 1);
    versionRepository.save(version);
  }

  public DataVersion getCurrentDataVersions() {
    Map<String, Long> currentVersions = versionRepository.findAll().stream()
        .collect(Collectors.toMap(
            MasterDataVersion::getDataType,
            MasterDataVersion::getVersion
        ));
    return new DataVersion(currentVersions);
  }

  public List<String> getStaleCategories(DataVersion clientVersion) {
    Map<String, Long> clientVersions = clientVersion.versions();
    Map<String, Long> serverVersions = getCurrentDataVersions().versions();
    List<String> staleCategories = new ArrayList<>();

    serverVersions.forEach((category, serverVer) -> {
      long clientVer = clientVersions.getOrDefault(category, 0L);
      if (serverVer > clientVer) {
        staleCategories.add(category);
      }
    });
    return staleCategories;
  }
}