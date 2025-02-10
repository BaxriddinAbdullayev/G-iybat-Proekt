package api.giybat.uz.entity;

import api.giybat.uz.enums.SmsType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "email_history")
public class EmailHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "email")
    private String email;

    @Column(name = "code")
    private String code;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_type")
    private SmsType emailType;

    @Column(name = "attempt_count")
    private Integer attemptCount;

}
