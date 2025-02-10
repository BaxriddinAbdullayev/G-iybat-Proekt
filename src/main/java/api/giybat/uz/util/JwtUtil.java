package api.giybat.uz.util;

import api.giybat.uz.dto.JwtDTO;
import api.giybat.uz.enums.ProfileRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JwtUtil {

    private static final int tokenLiveTime = 1000 * 3600 * 24; // 1-day
    private static final String secretKey = "veryLongSecretmazgillattayevlasharaaxmojonjinnijonsurbetbekkiydirhonuxlatdibekloxovdangasabekochkozjonduxovmashaynikmaydagapchishularnioqiganbolsangizgapyoqaniqsizmazgi";

    public static String encode(String username, Integer id, List<ProfileRole> roleList) {
        // ROLE_USER, ROLE_ADMIN
        String strRoles = roleList.stream().map(Enum::name)
                .collect(Collectors.joining(","));

//        List<String> stringList = new LinkedList<>();
//        for (ProfileRole role : roleList) {
//            stringList.add(role.name());
//        }
//        String roleString = String.join(",", stringList);

        Map<String, String> claims = new HashMap<>();
        claims.put("roles", strRoles);
        claims.put("id", String.valueOf(id));

        return Jwts
                .builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenLiveTime))
                .signWith(getSignInKey())
                .compact();
    }

    public static JwtDTO decode(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String username = claims.getSubject();
        Integer id = Integer.valueOf((String) claims.get("id"));
        String srtRole = (String) claims.get("roles");

        // ROLE_USER, ROLE_ADMIN
//        String[] roleArray = srtRole.split(",");
//        List<ProfileRole> roleList = new ArrayList<>();
//        for (String role : roleArray) {
//            roleList.add(ProfileRole.valueOf(role));
//        }


        List<ProfileRole> roleList = Arrays.stream(srtRole.split(","))
                .map(ProfileRole::valueOf)
                .toList();
        return new JwtDTO(username,id, roleList);
    }

    public static String encode(Integer id) {
        return Jwts
                .builder()
                .subject(String.valueOf(id))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (60 * 60 * 1000)))
                .signWith(getSignInKey())
                .compact();
    }

    public static Integer decodeRegVerToken(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Integer.valueOf(claims.getSubject());
    }

    private static SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
