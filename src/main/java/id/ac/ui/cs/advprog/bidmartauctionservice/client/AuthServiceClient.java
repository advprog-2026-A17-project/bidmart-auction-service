package id.ac.ui.cs.advprog.bidmartauctionservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8080}")
    private String authServiceUrl;

    public boolean hasPermission(String email, String permission) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(authServiceUrl + "/api/v1/auth/permissions/check")
                .queryParam("email", email)
                .queryParam("permission", permission)
                .build(true)
                .toUri();

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        return response != null && Boolean.TRUE.equals(response.get("allowed"));
    }
}
