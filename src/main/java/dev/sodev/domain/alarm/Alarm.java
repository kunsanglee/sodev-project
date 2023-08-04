package dev.sodev.domain.alarm;

import dev.sodev.domain.BaseEntity;
import dev.sodev.domain.enums.AlarmType;
import dev.sodev.domain.member.Member;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

@Builder
@Getter
@Entity
@Table(name = "alarm", indexes = {
        @Index(name = "member_id_idx", columnList = "member_id")
})
//@SQLDelete(sql = "UPDATE alarm SET removed_at = NOW() WHERE id=?")
//@Where(clause = "removed_at is NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Alarm extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private AlarmType type;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private AlarmArgs args;

    public static Alarm of(Member member, AlarmType alarmType, AlarmArgs args) {
        return Alarm.builder()
                .member(member)
                .type(alarmType)
                .args(args)
                .build();
    }
}