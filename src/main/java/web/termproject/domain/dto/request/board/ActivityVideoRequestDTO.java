package web.termproject.domain.dto.request.board;

import lombok.*;
import web.termproject.domain.entity.Board;
import web.termproject.domain.status.BoardType;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityVideoRequestDTO {
    private String title;
    private String content;//url
    private BoardType boardType;
    private String writer;

    public Board toEntity() {
        return Board.builder()
                .title(title)
                .writer(writer)
                .content(content)
                .boardType(BoardType.ACTIVITY_VIDEO)
                .build();
    }
}
