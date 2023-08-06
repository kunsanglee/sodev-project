package dev.sodev.global.kafka.event;

import dev.sodev.domain.alarm.AlarmArgs;
import dev.sodev.domain.enums.AlarmType;
import dev.sodev.domain.member.Member;
import dev.sodev.domain.project.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
public record AlarmEvent(
        Long memberId, Long projectId, List<Long> receiversId, AlarmType alarmType, AlarmArgs args
) {
    public static AlarmEvent of(AlarmType alarmType, Member member, Project project, List<Member> receivers) {

        AlarmArgs args;
        if (project == null) {
            args = new AlarmArgs(member.getId(), member.getNickName(), null, null);
        } else {
            args = new AlarmArgs(member.getId(), member.getNickName(), project.getId(), project.getTitle());
        }

        return AlarmEvent.builder()
                .memberId(member.getId())
                .projectId(project.getId())
                .receiversId(receivers.stream().map(Member::getId).toList())
                .alarmType(alarmType)
                .args(args)
                .build();
    }
}
