package dev.sodev.domain.project.service;

import dev.sodev.domain.project.repository.ProjectRepository;
import dev.sodev.domain.project.repository.ProjectSkillCustomRepositoryImpl;
import dev.sodev.domain.skill.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;

@Transactional
@SpringBootTest
class ProjectServiceImplTest {
    @MockBean
    SkillRepository skillRepository;
    @MockBean
    ProjectRepository projectRepository;
    @MockBean
    ProjectSkillBulkInsert projectSkillBulkInsert;
    @MockBean
    ProjectSkillCustomRepositoryImpl projectSkillRepository;
    @Autowired
    ProjectService projectService;

    @Test
    void selectProject() {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        String str = "2022-05-29 07:10:59";
//        LocalDateTime date = LocalDateTime.parse(str, formatter);
//        String[] set = {"a", "b", "c"};
//        List<String> skills = new ArrayList<String>(List.of(set));
//        ProjectInfoRequest request = new ProjectInfoRequest(3,3, date, date,date,"test","test",skills);
//        Long Id = 1L;
//        when(Project.of(request)).thenReturn(Project.of(request));
//        OngoingStubbing<Project> projectOngoingStubbing = when(projectRepository.save(Project.of(request))).thenReturn(1L);
//        when(projectService.findAndSaveSkill(request.skillSet())).thenReturn(any());
//        when(projectSkillBulkInsert.saveAll())

    }

    @Test
    void createProject() {
    }

    @Test
    void findAndSaveSkill() {
    }
}