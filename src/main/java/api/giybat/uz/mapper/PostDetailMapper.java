package api.giybat.uz.mapper;

import java.time.LocalDateTime;

public interface PostDetailMapper {
    String getPostId();

    String getPostTitle();

    String getPostPhotoId();

    LocalDateTime getPostCreatedDate();

    Integer getProfileId();

    String getProfileName();

    String getProfileUsername();
}
