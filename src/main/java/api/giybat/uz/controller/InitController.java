//package api.giybat.uz.controller;
//
//import api.giybat.uz.dto.PostDTO;
//import api.giybat.uz.entity.ProfileEntity;
//import api.giybat.uz.enums.GeneralStatus;
//import api.giybat.uz.enums.ProfileRole;
//import api.giybat.uz.repository.ProfileRepository;
//import api.giybat.uz.service.ProfileRoleService;
//import api.giybat.uz.service.ProfileService;
//import api.giybat.uz.util.SpringSecurityUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/init")
//@RequiredArgsConstructor
//public class InitController {
//
//    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//    private final ProfileRepository profileRepository;
//    private final ProfileRoleService profileRoleService;
//
//    @GetMapping("/all")
//    public String updateDetail(){
//
//        Optional<ProfileEntity> exists = profileRepository.findByUsernameAndVisibleTrue("adminjon@gmail.com");
//        if(exists.isPresent()){
//            return "Present";
//        }
//
//        ProfileEntity profile = new ProfileEntity();
//        profile.setName("Admin");
//        profile.setUsername("adminjon@gmail.com");
//        profile.setVisible(true);
//        profile.setPassword(bCryptPasswordEncoder.encode("123456"));
//        profile.setStatus(GeneralStatus.ACTIVE);
//        profile.setCreatedDate(LocalDateTime.now());
//
//        profileRepository.save(profile);
//        profileRoleService.create(profile.getId(), ProfileRole.ROLE_ADMIN);
//        profileRoleService.create(profile.getId(), ProfileRole.ROLE_USER);
//        return "DONE";
//    }
//}
