package api.giybat.uz.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfilePhotoUpdateDTO {

    @NotBlank(message = "PhotoId required")
    private String photoId;
}
