package dev.sodev.domain.member.repository.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.sodev.domain.enums.ProjectRole;
import dev.sodev.domain.enums.ProjectState;
import dev.sodev.domain.member.dto.MemberAppliedDto;
import dev.sodev.domain.member.dto.MemberHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dev.sodev.domain.member.QMemberProject.*;
import static dev.sodev.domain.project.QProject.*;

@Repository
@RequiredArgsConstructor
public class MemberProjectCustomRepositoryImpl implements MemberProjectCustomRepository{

    private final JPAQueryFactory queryFactory;

    public void deleteAllByApplicantId(Long memberId) {
        queryFactory.delete(memberProject)
                .where(memberProject.member.id.eq(memberId).and(memberProject.projectRole.role.eq(ProjectRole.Role.APPLICANT)))
                .execute();
    }

    @Override
    public Slice<MemberAppliedDto> findAppliedProjectsByMemberId(Long memberId, Pageable pageable) {
        List<MemberAppliedDto> result = queryFactory.select(Projections.constructor(MemberAppliedDto.class, project.id, project.title))
                .from(memberProject)
                .join(memberProject.project, project)
                .where(memberProject.member.id.eq(memberId).and(memberProject.projectRole.role.eq(ProjectRole.Role.APPLICANT)))
                .orderBy(project.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return getResultSlice(pageable, result);
    }

    @Override
    public Slice<MemberHistoryDto> findHistoryProjectsByMemberId(Long memberId, Pageable pageable) {
        List<MemberHistoryDto> result = queryFactory.select(Projections.constructor(MemberHistoryDto.class, project.id, project.title))
                .from(memberProject)
                .join(memberProject.project, project)
                .on(project.state.eq(ProjectState.COMPLETE))
                .where(memberProject.member.id.eq(memberId).and(memberProject.projectRole.role.eq(ProjectRole.Role.MEMBER).or(memberProject.projectRole.role.eq(ProjectRole.Role.CREATOR))))
                .orderBy(project.endDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (result.size() == pageable.getPageSize() + 1) {
            hasNext = true;
            result.remove(result.size() - 1);
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }

    private static SliceImpl<MemberAppliedDto> getResultSlice(Pageable pageable, List<MemberAppliedDto> result) {
        boolean hasNext = false;
        if (result.size() == pageable.getPageSize() + 1) {
            hasNext = true;
            result.remove(result.size() - 1);
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }
}
