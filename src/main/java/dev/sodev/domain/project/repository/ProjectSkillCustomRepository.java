package dev.sodev.domain.project.repository;

import dev.sodev.domain.enums.SearchType;
import dev.sodev.domain.project.dto.ProjectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface ProjectSkillCustomRepository {

    Optional<ProjectDto> findProject(Long projectId);
    void saveAll(List<Integer> skills, Long projectId);

    Slice<ProjectDto> searchAll(Pageable pageable);

    Slice<ProjectDto> search(SearchType searchType, String keyword, List<String> skillSet, Pageable pageable);


}
