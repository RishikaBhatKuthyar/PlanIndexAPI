package com.advancedBigDataIndexing.PlanService.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Component
public class GoogleTokenVerifier {

    private static final String CLIENT_ID = "364192984021-3a0m4tbqav53hgknn0b2uelmcgo15ja6.apps.googleusercontent.com";
    private static final String GOOGLE_PUBLIC_KEYS_URL = "https://www.googleapis.com/oauth2/v1/certs";

    private final RestTemplate restTemplate;
    private Map<String, Object> publicKeys;

    public GoogleTokenVerifier(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GoogleIdToken.Payload verifyToken(String token) throws Exception {
        fetchGooglePublicKeys(); // Fetch public keys before verification

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(token);
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new Exception("Invalid ID token.");
        }
    }

    public void fetchGooglePublicKeys() throws Exception {
        ResponseEntity<Map> response = restTemplate.getForEntity(GOOGLE_PUBLIC_KEYS_URL, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            publicKeys = response.getBody();
        } else {
            throw new Exception("Failed to fetch Google public keys");
        }
    }

    public Map<String, Object> getPublicKeys() {
        return publicKeys;
    }
}
