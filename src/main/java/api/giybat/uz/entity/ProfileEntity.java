package api.giybat.uz.entity;

import api.giybat.uz.enums.GeneralStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "profile")
public class ProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "username")
    private String username;

    @Column(name = "temp_username")
    private String tempUsername;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private GeneralStatus status;

    @Column(name = "visible")
    private Boolean visible = Boolean.TRUE;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "photo_id")
    private String photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", insertable = false, updatable = false)
    private AttachEntity photo;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY)
    private List<ProfileRoleEntity> roleList;

}
