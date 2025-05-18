package api.giybat.uz.dto;

import api.giybat.uz.dto.attach.AttachDTO;
import api.giybat.uz.enums.GeneralStatus;
import api.giybat.uz.enums.ProfileRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ProfileDTO {
    private Integer id;
    private String name;
    private String username;
    private List<ProfileRole> roleList;
    private String jwt;
    private AttachDTO photo;
    private GeneralStatus status;
    private LocalDateTime createdDate;
    private Long postCount;
 }
