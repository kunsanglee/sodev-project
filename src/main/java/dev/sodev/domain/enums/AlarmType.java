package dev.sodev.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlarmType {
    LIKE_ON_FEED("new like!"),
    COMMENT_ON_FEED("new comment!"),
    COMMENT_ON_COMMENT("new comment on your comment!"),
    APPLICANT_ON_FEED("new applicant!"),
    FEED_CREATED("new feed!"),
    FEED_UPDATED("feed updated!"),
    NEW_MEMBER_JOINED("new member joined!"),
    TEAM_JOIN_FAILED("team join failed."),
    MEMBER_KICKED_OUT("team member kicked out."),
    PROJECT_STARTED("your project started!"),
    PROJECT_COMPLETED("your project completed!"),
    PEER_REVIEW_CREATED("your peer review created!")
    ;

    private final String alarmText;
}
