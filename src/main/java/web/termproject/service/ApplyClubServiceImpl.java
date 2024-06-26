package web.termproject.service;

import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.termproject.domain.dto.request.ApplyClubRequestDTO;
import web.termproject.domain.dto.response.ApplyClubResponseDTO;
import web.termproject.domain.dto.response.ClubResponseDTO;
import web.termproject.domain.dto.response.MemberResponseDTO;
import web.termproject.domain.dto.response.ProfessorResponseDTO;
import web.termproject.domain.entity.*;
import web.termproject.domain.status.ApplyClubStatus;
import web.termproject.repository.ApplyClubRepository;
import web.termproject.repository.ClubRepository;
import web.termproject.repository.MemberRepository;
import web.termproject.repository.ProfessorRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ApplyClubServiceImpl implements ApplyClubService {

    private final MemberRepository memberRepository;
    private final ProfessorRepository professorRepository;
    private final ApplyClubRepository applyClubRepository;
    private final ClubRepository clubRepository;
    private final ModelMapper modelMapper;

    @Override
    public ApplyClubResponseDTO createApplyClub(ApplyClubRequestDTO requestDTO) {
        Member member = memberRepository.findByName(requestDTO.getName());
        Professor professor = professorRepository.findByName(requestDTO.getPName());
        if(member == null) {
            throw new UsernameNotFoundException("존재하지 않는 학생입니다.");
        }
        if(professor == null) {
            throw new UsernameNotFoundException("존재하지 않는 교수입니다.");
        }

        Optional<ApplyClub> existingApplyClub = applyClubRepository.findByClubNameAndMember_StuNum(requestDTO.getClubName(), requestDTO.getStuNum());
        if (existingApplyClub.isPresent()) {
            throw new DuplicateRequestException("이미 해당 동아리 신청이 존재합니다.");
        }
        ApplyClub applyClub = ApplyClub.createApplyClub(requestDTO.getClubType(), requestDTO.getClubName(), member, professor);

        ApplyClub savedApplyClub  = applyClubRepository.save(applyClub);
        return ApplyClubResponseDTO.builder()
                .applyClubId(savedApplyClub.getId())
                .applyClubStatus(savedApplyClub.getApplyClubStatus())
                .clubType(savedApplyClub.getClubType())
                .clubName(savedApplyClub.getClubName())
                .name(savedApplyClub.getMember().getName())
                .major(savedApplyClub.getMember().getMajor())
                .stuNum(savedApplyClub.getMember().getStuNum())
                .phoneNum(savedApplyClub.getMember().getPhoneNum())
                .pName(savedApplyClub.getProfessor().getName())
                .pMajor(savedApplyClub.getProfessor().getMajor())
                .pPhoneNum(savedApplyClub.getProfessor().getPhoneNum())
                .refuseReason(savedApplyClub.getRefuseReason())
                .build();
    }

    @Override
    public ApplyClub createApplyClubEntity(ApplyClubRequestDTO requestDTO) {
        Member member = memberRepository.findByName(requestDTO.getName());
        Professor professor = professorRepository.findByName(requestDTO.getPName());
        if(member == null) {
            throw new UsernameNotFoundException("존재하지 않는 학생입니다.");
        }
        if(professor == null) {
            throw new UsernameNotFoundException("존재하지 않는 교수입니다.");
        }

        Optional<ApplyClub> existingApplyClub = applyClubRepository.findByClubNameAndMember_StuNum(requestDTO.getClubName(), requestDTO.getStuNum());
        if (existingApplyClub.isPresent()) {
            throw new DuplicateRequestException("이미 해당 동아리 신청이 존재합니다.");
        }
        ApplyClub applyClub = ApplyClub.createApplyClub(requestDTO.getClubType(), requestDTO.getClubName(), member, professor);

        return applyClubRepository.save(applyClub);
    }

    @Override
    public ClubResponseDTO createClub(ApplyClub applyClub) {
        Club club = new Club().createClub(applyClub); // 수정: 기존 Club 인스턴스를 이용하지 않고, 새 Club 인스턴스를 반환 받습니다.
        club.setApplyClub(applyClub);
        clubRepository.save(club);

        ClubResponseDTO responseDTO = ClubResponseDTO.builder()
                .id(club.getId())
                .applyClubId(club.getApplyClub().getId())
                .clubType(club.getClubType())
                .name(club.getName())
                .build();
        responseDTO.setProfessor(modelMapper.map(applyClub.getProfessor(), ProfessorResponseDTO.class));
        responseDTO.setMasterMember(modelMapper.map(applyClub.getMember(), MemberResponseDTO.class));

        List<ApplyMember> applyMemberList = club.getApplyMemberList();
        for (ApplyMember applyMember : applyMemberList) {
            log.info("applyMember : {}", applyMember.getMember().getName());
        }

        return responseDTO;
    }

    @Override
    public ApplyClubResponseDTO refuseClub(Long applyClubId, String refuseReason) {
        ApplyClub applyClub = applyClubRepository.findById(applyClubId)
                .orElseThrow(() -> new IllegalArgumentException("동아리 신청내역이 존재하지 않습니다."));
        applyClub.setApplyClubStatus(ApplyClubStatus.REFUSE);
        applyClub.setRefuseReason(refuseReason);
        ApplyClub savedApplyClub = applyClubRepository.save(applyClub);
        return ApplyClubResponseDTO.builder()
                .applyClubId(savedApplyClub.getId())
                .applyClubStatus(savedApplyClub.getApplyClubStatus())
                .clubType(savedApplyClub.getClubType())
                .clubName(savedApplyClub.getClubName())
                .name(savedApplyClub.getMember().getName())
                .major(savedApplyClub.getMember().getMajor())
                .stuNum(savedApplyClub.getMember().getStuNum())
                .phoneNum(savedApplyClub.getMember().getPhoneNum())
                .pName(savedApplyClub.getProfessor().getName())
                .pMajor(savedApplyClub.getProfessor().getMajor())
                .pPhoneNum(savedApplyClub.getProfessor().getPhoneNum())
                .refuseReason(savedApplyClub.getRefuseReason())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ApplyClubResponseDTO> findAll() {
        List<ApplyClub> applyClubList = applyClubRepository.findAll();
        List<ApplyClubResponseDTO> responseDTOS = new ArrayList<>();

        for (ApplyClub applyClub : applyClubList) {
            ApplyClubResponseDTO responseDTO = ApplyClubResponseDTO.builder()
                    .applyClubId(applyClub.getId())
                    .applyClubStatus(applyClub.getApplyClubStatus())
                    .clubType(applyClub.getClubType())
                    .clubName(applyClub.getClubName())
                    .name(applyClub.getMember().getName())
                    .major(applyClub.getMember().getMajor())
                    .stuNum(applyClub.getMember().getStuNum())
                    .phoneNum(applyClub.getMember().getPhoneNum())
                    .pName(applyClub.getProfessor().getName())
                    .pMajor(applyClub.getProfessor().getMajor())
                    .pPhoneNum(applyClub.getProfessor().getPhoneNum())
                    .build();
            responseDTOS.add(responseDTO);
        }
        return responseDTOS;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ApplyClubResponseDTO> findApplyClubByMember(String loginId) {
        List<ApplyClub> applyClubList = applyClubRepository.findAll();
        List<ApplyClubResponseDTO> responseDTOS = new ArrayList<>();

        Member member = memberRepository.findByLoginId(loginId);

        for (ApplyClub applyClub : applyClubList) {
            if(applyClub.getMember() == member) {
                ApplyClubResponseDTO responseDTO = ApplyClubResponseDTO.builder()
                        .applyClubId(applyClub.getId())
                        .applyClubStatus(applyClub.getApplyClubStatus())
                        .clubType(applyClub.getClubType())
                        .clubName(applyClub.getClubName())
                        .name(applyClub.getMember().getName())
                        .major(applyClub.getMember().getMajor())
                        .stuNum(applyClub.getMember().getStuNum())
                        .phoneNum(applyClub.getMember().getPhoneNum())
                        .pName(applyClub.getProfessor().getName())
                        .pMajor(applyClub.getProfessor().getMajor())
                        .pPhoneNum(applyClub.getProfessor().getPhoneNum())
                        .refuseReason(applyClub.getRefuseReason())
                        .build();
                responseDTOS.add(responseDTO);
            }
        }
        return responseDTOS;
    }

    @Override
    public ApplyClub findById(Long id) {
        return applyClubRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("동아리 신청내역이 존재하지 않습니다."));
    }

    @Override
    public void save(ApplyClub applyClub) {
        applyClubRepository.save(applyClub);
    }

    @Override
    public List<ApplyClub> findByApplyClubStatus(ApplyClubStatus applyClubStatus) {
        return applyClubRepository.findByApplyClubStatus(applyClubStatus);
    }
}
