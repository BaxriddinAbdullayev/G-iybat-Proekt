package api.giybat.uz.dto.post;

import api.giybat.uz.dto.ProfileDTO;
import api.giybat.uz.dto.attach.AttachDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostDTO {
    private String id;
    private String title;
    private String content;
    private AttachDTO photo;
    private LocalDateTime createdDate;
    private ProfileDTO profile;
}
