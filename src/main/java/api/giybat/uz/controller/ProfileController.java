package api.giybat.uz.controller;

import api.giybat.uz.dto.AppResponse;
import api.giybat.uz.dto.CodeConfirmDTO;
import api.giybat.uz.dto.ProfileDTO;
import api.giybat.uz.dto.profile.*;
import api.giybat.uz.enums.AppLanguage;
import api.giybat.uz.service.ProfileService;
import api.giybat.uz.util.PageUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/filter")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<PageImpl<ProfileDTO>> filter(
            @Valid @RequestBody ProfileFilterDTO dto,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang
    ) {
        return ResponseEntity.ok(profileService.filter(dto, PageUtil.page(page), size, lang));
    }

    @PutMapping("/photo")
    public ResponseEntity<AppResponse<String>> updatePhoto(
            @Valid @RequestBody ProfilePhotoUpdateDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang
    ) {
        return ResponseEntity.ok(profileService.updatePhoto(dto.getPhotoId(), lang));
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<String>> status(
            @PathVariable("id") Integer id,
            @RequestBody ProfileStatusDTO dto,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang
    ) {
        return ResponseEntity.ok(profileService.changeStatus(id, dto.getStatus(), lang));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AppResponse<String>> delete(
            @PathVariable("id") Integer id,
            @RequestHeader(value = "Accept-Language", defaultValue = "UZ") AppLanguage lang
    ) {
        return ResponseEntity.ok(profileService.delete(id, lang));
    }
}
