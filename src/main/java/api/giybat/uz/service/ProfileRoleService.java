package api.giybat.uz.service;

import api.giybat.uz.entity.ProfileRoleEntity;
import api.giybat.uz.enums.ProfileRole;
import api.giybat.uz.repository.ProfileRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileRoleService {

    private final ProfileRoleRepository profileRoleRepository;

    public void create(Integer profileId, ProfileRole role  ) {
        ProfileRoleEntity entity = new ProfileRoleEntity();
        entity.setProfileId(profileId);
        entity.setRoles(role);
        entity.setCreatedDate(LocalDateTime.now());
        profileRoleRepository.save(entity);
    }

    public void deleteRoles(Integer profileId){
        profileRoleRepository.deleteByProfileId(profileId);
    }
}
