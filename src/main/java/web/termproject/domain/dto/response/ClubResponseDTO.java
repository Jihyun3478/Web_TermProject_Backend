package web.termproject.domain.dto.response;

import lombok.Getter;
import lombok.Setter;
import web.termproject.domain.status.ClubType;

import java.util.Date;

@Getter
@Setter
public class ClubResponseDTO {
    private Long id;
    private ClubType clubType;
    private String name;
    private String introduce;
    private String imageRoute;
    private Date meetingTime;
    private String president;
    private String vicePresident;
    private String generalAffairs;
}
