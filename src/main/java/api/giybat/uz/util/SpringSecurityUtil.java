package api.giybat.uz.util;

import api.giybat.uz.config.CustomUserDetails;
import api.giybat.uz.enums.ProfileRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class SpringSecurityUtil {

    public static CustomUserDetails getCurrentProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public static Integer getCurrentUserId(){
        CustomUserDetails user = getCurrentProfile();
        return user.getId();
    }

    public static boolean hasRole(ProfileRole requiredRole){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(sga -> sga.getAuthority().equals(requiredRole.name()));
    }
}
