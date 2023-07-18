
package dev.sodev.domain.project.service;

import dev.sodev.domain.member.Member;
import dev.sodev.domain.member.MemberProject;
import dev.sodev.domain.member.repository.MemberProjectRepository;
import dev.sodev.domain.member.repository.MemberRepository;
import dev.sodev.domain.member.service.MemberService;
import dev.sodev.domain.project.Project;
import dev.sodev.domain.project.dto.requset.ProjectInfoRequest;
import dev.sodev.domain.project.dto.response.ProjectResponse;
import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.domain.project.repository.ProjectSkillCustomRepositoryImpl;
import dev.sodev.domain.project.repository.ProjectSkillRepository;
import dev.sodev.domain.skill.Skill;
import dev.sodev.domain.skill.repository.SkillRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest
class ProjectServiceImplTest {

    @MockBean
    SkillRepository skillRepository;
    @MockBean
    ProjectRepository projectRepository;
    @MockBean
    MemberProjectRepository memberProjectRepository;
    @MockBean
    ProjectSkillRepository projectSkillRepository;
    @MockBean
    MemberRepository memberRepository;
    @Autowired
    MemberService memberService;
    @Autowired
    ProjectService projectService;

    @Test
    @DisplayName("게시글 작성 성공할경우")
    void insertProject() {
        // fixture
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String str = "2022-05-29 07:10:59";
//        LocalDateTime date = LocalDateTime.parse(str, formatter);
//        String[] set = {"자바", "스프링", "노드"};
//        List<String> skills = new ArrayList<String>(List.of(set));
//        ProjectInfoRequest request = new ProjectInfoRequest(3, 3,date, date,date,"test","test",skills);
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Member member = Member.builder().id(1L).email("a@naver.com").build();
//        List<Skill> skillList = new ArrayList<Skill>();
//        Long Id = 1L;
//        when(memberRepository.getReferenceByEmail(authentication.getName())).thenReturn(member);
//        when(projectRepository.save(Project.of(request)));
//        when(memberProjectRepository.save(MemberProject.of(member,Project.of(request))));
//        when(projectService.findAndSaveSkill(request.skillSet())).thenReturn(skillList);
//        when(projectSkillRepository.saveAll(skillList,Project.of(request).getId()))
//        memberService.join()
//        ProjectResponse response = projectService.createProject(request);
//        Assertions.assertEquals(response.project().get(0).getContent(), "test");



    }

    @Test
    void createProject() {
    }

    @Test
    void findAndSaveSkill() {
    }
}

