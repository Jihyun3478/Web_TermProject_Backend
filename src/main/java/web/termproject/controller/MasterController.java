package web.termproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.termproject.domain.dto.request.ClubRequestDTO;
import web.termproject.domain.dto.response.ApplyMemberReponseDTO;
import web.termproject.domain.dto.response.ClubResponseDTO;
import web.termproject.domain.status.ApplyMemberStatus;
import web.termproject.service.MasterService;

import java.util.List;

/**
 * 마스터 관리자 Controller
 *
 * 동아리 승인 이후의 시나리오로 작성함
 */
@RestController
@RequestMapping("/master/club")
@RequiredArgsConstructor
public class MasterController {
    private final MasterService masterService;

    @GetMapping("/list/{memberId}")
    public List<ClubResponseDTO> clubs(@PathVariable("memberId") Long memberId) {
        return masterService.getMasterClubsInfo(memberId);
    }

    @GetMapping("/{clubId}")
    public ClubResponseDTO club(@PathVariable("clubId") Long clubId, @RequestParam("memberId") Long memberId) {
        return masterService.findMasterClubInfo(clubId, memberId);
    }

    @PostMapping("/{clubId}")
    public ClubResponseDTO updateMasterClubInfo(@PathVariable("clubId") Long clubId, @RequestParam("memberId") Long memberId, @RequestBody ClubRequestDTO clubRequestDTO) {
        return masterService.updateMasterClubInfo(clubId, memberId, clubRequestDTO);
    }

    @GetMapping("/applyMember/{clubId}")
    public List<ApplyMemberReponseDTO> getApplyMemberList(@PathVariable("clubId") Long clubId) {
        return masterService.getApplyMemberList(clubId);
    }

    @PostMapping("/applyMember/")
    public ResponseEntity<String> updateApplyMemberStatus(@RequestParam("applyMemberId") Long applyMemberId, @RequestParam("ApplyMemberStatus")ApplyMemberStatus applyMemberStatus) {
        return masterService.updateApplyMemberStatus(applyMemberId, applyMemberStatus);
    }
}
