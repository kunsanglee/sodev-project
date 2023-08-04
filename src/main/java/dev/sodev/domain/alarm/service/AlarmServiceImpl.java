package dev.sodev.domain.alarm.service;

import dev.sodev.domain.alarm.AlarmArgs;
import dev.sodev.domain.alarm.dto.AlarmDto;
import dev.sodev.domain.alarm.repository.AlarmRepository;
import dev.sodev.domain.enums.AlarmType;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.follow.Follow;
import dev.sodev.domain.likes.Likes;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.global.exception.ErrorCode;
import dev.sodev.global.exception.SodevApplicationException;
import dev.sodev.global.security.utils.SecurityUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;
    private final ProjectRepository projectRepository;
    private final EntityManager em;

    @Override
    public Slice<AlarmDto> alarmList(Pageable pageable) {
        String memberEmail = SecurityUtil.getMemberEmail();
        Member member = memberRepository.findByEmail(memberEmail).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));

        return alarmRepository.findAllByMember(member, pageable).map(AlarmDto::of);
    }

    @Override
    @Transactional
    public void alarmsToMember(Long memberId, Long projectId, AlarmType alarmType) {
        clear();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));

        List<Member> members = project.getMembers().stream()
                .filter(mp -> !mp.getProjectRole().getRole().equals(ProjectRole.Role.APPLICANT))
                .map(MemberProject::getMember)
                .toList();

        sendAlarms(alarmType, member, project, members);
    }

    @Override
    @Transactional
    public void alarmsToFollower(Long memberId, Long projectId, AlarmType alarmType) {
        clear();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));

        List<Member> followers = member.getFollowers().stream().map(Follow::getFromMember).toList();
        sendAlarms(alarmType, member, project, followers);
    }

    @Override
    @Transactional
    public void alarmsToLikes(Long memberId, Long projectId, AlarmType alarmType) {
        clear();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new SodevApplicationException(ErrorCode.MEMBER_NOT_FOUND));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new SodevApplicationException(ErrorCode.FEED_NOT_FOUND));

        List<Member> likeMembers = project.getLikes().stream().map(Likes::getMember).toList();
        sendAlarms(alarmType, member, project, likeMembers);
    }

    private void sendAlarms(AlarmType alarmType, Member member, Project project, List<Member> members) {
        alarmRepository.bulkAlarmsSave(members, alarmType, new AlarmArgs(member.getId(), project.getId()));
    }

    private void clear() {
        em.flush();
        em.clear();
    }
}
