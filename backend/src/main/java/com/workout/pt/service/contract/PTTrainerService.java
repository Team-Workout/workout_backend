package com.workout.pt.service.contract;

import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.FileErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.dto.response.ClientListResponse.MemberResponse;
import com.workout.pt.repository.PTContractRepository;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.service.TrainerService;
import com.workout.utils.domain.UserFile;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.service.FileService;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PTTrainerService {

  private final PTContractRepository ptContractRepository;
  private final TrainerService trainerService;
  private final FileService fileService; // <-- 의존성 추가
  private final MemberService memberService;

  public PTTrainerService(PTContractRepository ptContractRepository,
      TrainerService trainerService, FileService fileService, MemberService memberService) {
    this.ptContractRepository = ptContractRepository;
    this.trainerService = trainerService;
    this.fileService = fileService;
    this.memberService = memberService;
  }

  public Page<FileResponse> findMemberBodyImagesByTrainer(Long trainerId, Long memberId,
      LocalDate startDate, LocalDate endDate, Pageable pageable) {

    // 1. 권한 검증 로직
    trainerService.findById(trainerId); // 트레이너 존재 여부 확인

    if (!this.isMyClient(trainerId, memberId)) { // isMyClient는 PTTrainerService 자신의 메소드
      throw new RestApiException(FileErrorCode.NOT_AUTHORITY);
    }

    Member member = memberService.findById(memberId);
    // Member 엔티티에 isOpenBodyImg 필드가 없으므로, isOpenWorkoutRecord로 검사하거나
    // 혹은 필요한 필드를 Member 엔티티에 추가해야 합니다. 여기서는 isOpenWorkoutRecord로 가정합니다.
    if (!member.getIsOpenWorkoutRecord()) {
      throw new RestApiException(FileErrorCode.NOT_AUTHORITY);
    }

    // 2. 데이터 조회 위임
    Page<UserFile> userFilesPage = fileService.findBodyImagesByMember(
        memberId, startDate, endDate, pageable);

    return userFilesPage.map(FileResponse::from);
  }

  public Page<MemberResponse> findMyClients(Long trainerId, Pageable pageable) {

    Trainer trainer = trainerService.findById(trainerId);

    Page<PTContract> contractsPage = ptContractRepository
        .findByTrainerIdAndStatus(trainer.getId(), PTContractStatus.ACTIVE, pageable);

    return contractsPage.map(contract -> MemberResponse.from(contract.getMember()));
  }

  public boolean isMyClient(Long trainerId, Long memberId) {
    return ptContractRepository.existsByTrainerIdAndMemberIdAndStatus(trainerId, memberId,
        PTContractStatus.ACTIVE);
  }
}
