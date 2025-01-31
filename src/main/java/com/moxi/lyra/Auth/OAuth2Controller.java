package com.moxi.lyra.Auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.moxi.lyra.Config.Utils.JwtUtil;
import com.moxi.lyra.DTO.LoginRes;
import com.moxi.lyra.DTO.TokenRequest;
import com.moxi.lyra.User.Provider;
import com.moxi.lyra.User.User;
import com.moxi.lyra.User.UserRole.UserRole;
import com.moxi.lyra.User.UserRole.UserRoleRepository;
import com.moxi.lyra.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

@RestController
@RequestMapping("api/oauth2")
public class OAuth2Controller {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    @Autowired
    private UserRoleRepository userRoleRepository;

    @PostMapping("/google")
    public ResponseEntity<?> googleUser(@RequestBody TokenRequest tokenRequest) {
        String googleToken = tokenRequest.getToken();
        GoogleIdTokenVerifier verifier;
        try {
            verifier = new GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String username = payload.getSubject();
                String name = (String) payload.get("name");
                String picture = (String) payload.get("picture");
                User existingUser = userService.findByEmail(email);
                Date aujourdhui = new Date();
                SimpleDateFormat formatedDate = new SimpleDateFormat("dd-MM-yyyy");
                String dateString = formatedDate.format(aujourdhui);
                UserRole userRole = userRoleRepository.findById(1L).get();
                if (existingUser == null) {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setUsername(username);
                    newUser.setProvider(Provider.GOOGLE);
                    newUser.setDateInscription(dateString);
                    newUser.setUserRole(userRole);
                    userService.saveUser(newUser);
                    String jwtToken = jwtUtil.createAccessToken(newUser);
                    return ResponseEntity.ok(new LoginRes(newUser.getUsername(), jwtToken));
                } else {
                    String jwtToken = jwtUtil.createAccessToken(existingUser);
                    return ResponseEntity.ok(new LoginRes(existingUser.getUsername(), jwtToken));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Google token.");
            }
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying Google token.");
        }
    }
}
