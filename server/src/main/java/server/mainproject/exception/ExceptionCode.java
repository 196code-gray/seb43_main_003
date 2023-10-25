package server.mainproject.exception;

import lombok.Getter;

public enum ExceptionCode {
    MEMBER_NOT_FOUND(404, "회원이 존재하지 않습니다."),
    MEMBER_EXIST(402, "존재하는 회원입니다."),
    UNAUTHORIZED_MEMBER(401, "접근 권한이 없습니다."),
    UNAUTHORIZED_ACCESSTOKEN(401, "액세스토큰이 만료되었습니다."),
    EMAIL_EXIST(404,"이메일 중복! 다른 이메일을 사용해주세요!"),
    MAILKEY_MISMATCH(409, "메일키가 다릅니다."),
    PASSWORD_NOT_CORRECT(409, "잘못된 비밀번호입니다."),
    POST_NOT_FOUND(404, "게시글이 존재하지 않습니다."),
    POST_NOT_WRITE(401, "권한이 없습니다."),
    COMMENT_NOT_FOUND(404, "답변이 존재하지 않습니다."),
    COMMENT_NOT_MEMBER(400, "회원이 작성한 답변이 아닙니다."),
    ALREADY_LIKES(400, "이미 좋아요를 눌렀습니다.");

    @Getter
    private int status;
    @Getter
    private String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
