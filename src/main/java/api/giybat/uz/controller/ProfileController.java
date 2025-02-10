package api.giybat.uz.controller;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.CodeConfirmDTO;
import api.giybat.uz.dto.profile.ProfileDetailUpdateDTO;
import api.giybat.uz.dto.profile.ProfilePasswordUpdateDTO;
import api.giybat.uz.dto.profile.ProfilePhotoUpdateDTO;
import api.giybat.uz.dto.profile.ProfileUsernameUpdateDTO;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.service.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "ProfileController", description = "Api set for working with Profile")
public class ProfileController {

    private final ProfileService profileService;

    @PutMapping("/detail")
    public ResponseEntity<AppResponse<String>> create(
            @Valid @RequestBody ProfileDetailUpdateDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang
    ) {
        return ResponseEntity.ok(profileService.updateDetail(dto, lang));
    }

    @PutMapping("/password")
    public ResponseEntity<AppResponse<String>> updatePassword(
            @Valid @RequestBody ProfilePasswordUpdateDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang
    ) {
        return ResponseEntity.ok(profileService.updatePassword(dto, lang));
    }

    @PutMapping("/username")
    public ResponseEntity<AppResponse<String>> updateUsername(
            @Valid @RequestBody ProfileUsernameUpdateDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang
    ) {
        return ResponseEntity.ok(profileService.updateUsername(dto, lang));
    }

    @PutMapping("/username/confirm")
        public ResponseEntity<AppResponse<String>> updateUsernameConfirm(
            @Valid @RequestBody CodeConfirmDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang
    ) {
        return ResponseEntity.ok(profileService.updateUsernameConfirm(dto, lang));
    }

    @PutMapping("/photo")
    public ResponseEntity<AppResponse<String>> updatePhoto(
            @Valid @RequestBody ProfilePhotoUpdateDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang
    ) {
        return ResponseEntity.ok(profileService.updatePhoto(dto.getPhotoId(), lang));
    }
}
