package api.giybat.uz.controller;

import api.giybat.uz.dto.attach.AttachDTO;
import api.giybat.uz.service.AttachService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attach")
@Tag(name = "AttachController", description = "Api set for working with Attach")
public class AttachController {

    private final AttachService attachService;

    @PostMapping("/upload")
    public ResponseEntity<AttachDTO> create(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(attachService.upload(file));
    }

    @GetMapping("/open/{fileName}")
    public ResponseEntity<Resource> open(@PathVariable String fileName) {
        return attachService.open(fileName);
    }
}
