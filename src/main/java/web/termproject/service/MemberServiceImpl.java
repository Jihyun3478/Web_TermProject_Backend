package web.termproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import web.termproject.domain.dto.request.JwtTokenDTO;
import web.termproject.domain.dto.request.SignupRequestDTO;
import web.termproject.domain.dto.response.MemberResponseDTO;
import web.termproject.domain.entity.Member;
import web.termproject.domain.status.ApplyMemberStatus;
import web.termproject.domain.status.RoleType;
import web.termproject.repository.MemberRepository;
import web.termproject.security.service.JwtTokenProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;

    public MemberResponseDTO createMember(SignupRequestDTO signupRequestDTO) {
        Member member = signupRequestDTO.toEntity();
        member.addMemberAuthority();
        member.encodePassword(passwordEncoder);

        Member savedMember = memberRepository.save(member);
        return MemberResponseDTO.builder()
                .loginId(savedMember.getLoginId())
                .loginPw(savedMember.getLoginPw())
                .name(savedMember.getName())
                .stuNum(savedMember.getStuNum())
                .major(savedMember.getMajor())
                .phoneNum(savedMember.getPhoneNum())
                .email(savedMember.getEmail())
                .gender(savedMember.getGender())
                .birthDate(savedMember.getBirthDate())
                .build();
    }

    public MemberResponseDTO createMasterMember(SignupRequestDTO signupRequestDTO) {
        Member member = signupRequestDTO.toEntity();
        member.addMasterMemberAuthority();
        member.encodePassword(passwordEncoder);

        Member savedMember = memberRepository.save(member);
        return MemberResponseDTO.builder()
                .loginId(savedMember.getLoginId())
                .loginPw(savedMember.getLoginPw())
                .name(savedMember.getName())
                .stuNum(savedMember.getStuNum())
                .major(savedMember.getMajor())
                .phoneNum(savedMember.getPhoneNum())
                .email(savedMember.getEmail())
                .gender(savedMember.getGender())
                .birthDate(savedMember.getBirthDate())
                .build();
    }

    public JwtTokenDTO signIn(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        return jwtTokenProvider.generateToken(authentication);
    }

    public Member findByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId);
        if(member == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return member;
    }

    public Member processOAuthPostLogin(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String nickname = null;
        String email = null;

        if (attributes.containsKey("properties") && attributes.containsKey("kakao_account")) {
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            nickname = (String) properties.get("nickname");
            email = (String) kakaoAccount.get("email");
        }

        if (nickname == null || email == null) {
            throw new IllegalArgumentException("OAuth2User attributes are missing required fields");
        }

        Optional<Member> byEmail = memberRepository.findByEmail(email);
        if (!byEmail.isPresent()) {
            Member member = Member.builder()
                    .loginId(email)
                    .loginPw(passwordEncoder.encode(email))
                    .name(nickname)
                    .stuNum("20200930")
                    .major("컴퓨터소프트웨어공학과")
                    .phoneNum("010-0000-0000")
                    .email(email)
                    .gender("여")
                    .birthDate("2001-08-15")
                    .role(RoleType.MEMBER)
                    .build();
            memberRepository.save(member);
            return member;
        }
        return byEmail.get();
    }


    @Override
    public List<Long> findClubIdsByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null || member.getApplyMemberList()== null || member.getApplyMemberList().isEmpty()) {
            return Collections.emptyList();
        }
        return member.getApplyMemberList().stream()
                .filter(applyClub -> applyClub.getApplyMemberStatus() == ApplyMemberStatus.CLUB_MEMBER)
                .map(applyClub -> applyClub.getClub().getId())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findClubNamesByLoginId(String loginId) {
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null || member.getApplyMemberList() == null || member.getApplyMemberList().isEmpty()) {
            return Collections.emptyList();
        }
        return member.getApplyMemberList().stream()
                .filter(applyClub -> applyClub.getApplyMemberStatus() == ApplyMemberStatus.CLUB_MEMBER)
                .map(applyClub -> applyClub.getClub().getName())
                .collect(Collectors.toList());
    }

    @Override
    public Member findByName(String memberName) {
        return memberRepository.findByName(memberName);
    }

    @Override
    public void save(Member member) {
        memberRepository.save(member);
    }
}
