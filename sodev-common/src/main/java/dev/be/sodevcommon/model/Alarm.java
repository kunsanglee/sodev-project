package dev.be.sodevcommon.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

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

    @Column(columnDefinition = "jsonb")
    @Type(JsonType.class)
    private AlarmArgs args;

}