package api.giybat.uz.dto.profile;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfilePasswordUpdateDTO {

    @NotBlank(message = "CurrentPswd password required")
    private String currentPswd;

    @NotBlank(message = "NewPswd password required")
    private String newPswd;
}
