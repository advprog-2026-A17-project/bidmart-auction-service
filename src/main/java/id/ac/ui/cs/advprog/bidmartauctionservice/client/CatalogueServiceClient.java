package id.ac.ui.cs.advprog.bidmartauctionservice.client;

import id.ac.ui.cs.advprog.bidmartauctionservice.dto.catalogue.ListingSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogueServiceClient {

    private final RestTemplate restTemplate;

    @Value("${catalogue.service.url:http://localhost:8081}")
    private String catalogueServiceUrl;

    public ListingSummaryResponse getListing(UUID listingId) {
        String url = catalogueServiceUrl + "/api/v1/catalogue/listings/{listingId}/summary";
        try {
            ListingSummaryResponse response = restTemplate.getForObject(
                    url,
                    ListingSummaryResponse.class,
                    listingId
            );
            if (response == null) {
                throw new IllegalArgumentException("Listing not found: " + listingId);
            }
            return response;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IllegalArgumentException("Listing not found: " + listingId);
        }
    }
}
