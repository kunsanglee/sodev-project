package dev.be.sodevcommon.domain.entity;

import dev.be.sodevcommon.domain.enums.AlarmType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "alarm"/*, *//*indexes = {
        @Index(name = "user_id_idx", columnList = "user_id"*//*)
}*/)
//@SQLDelete(sql = "UPDATE alarm SET removed_at = NOW() WHERE id=?")
//@Where(clause = "removed_at is NULL")
@NoArgsConstructor
public class Alarm extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private AlarmType type;

//    @Type(JsonType.class)
//    @Column(columnDefinition = "jsonb")
//    private AlarmArgs args;

}