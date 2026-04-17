package id.ac.ui.cs.advprog.bidmartauctionservice.client;

import id.ac.ui.cs.advprog.bidmartauctionservice.dto.catalogue.ListingSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogueServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private CatalogueServiceClient catalogueServiceClient;

    @BeforeEach
    void setUp() {
        catalogueServiceClient = new CatalogueServiceClient(restTemplate);
        ReflectionTestUtils.setField(catalogueServiceClient, "catalogueServiceUrl", "http://catalogue-service:8081");
    }

    @Test
    void getListing_UsesCatalogueSummaryEndpoint() {
        UUID listingId = UUID.randomUUID();
        ListingSummaryResponse expected = ListingSummaryResponse.builder()
                .id(listingId.toString())
                .sellerId(UUID.randomUUID().toString())
                .status("ACTIVE")
                .build();

        when(restTemplate.getForObject(
                eq("http://catalogue-service:8081/api/v1/catalogue/listings/{listingId}/summary"),
                eq(ListingSummaryResponse.class),
                eq(listingId)
        )).thenReturn(expected);

        ListingSummaryResponse actual = catalogueServiceClient.getListing(listingId);

        assertEquals(expected, actual);
        verify(restTemplate).getForObject(
                eq("http://catalogue-service:8081/api/v1/catalogue/listings/{listingId}/summary"),
                eq(ListingSummaryResponse.class),
                eq(listingId)
        );
    }

    @Test
    void getListing_WhenCatalogueReturnsNull_ThrowsIllegalArgumentException() {
        UUID listingId = UUID.randomUUID();
        when(restTemplate.getForObject(
                eq("http://catalogue-service:8081/api/v1/catalogue/listings/{listingId}/summary"),
                eq(ListingSummaryResponse.class),
                eq(listingId)
        )).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> catalogueServiceClient.getListing(listingId)
        );

        assertEquals("Listing not found: " + listingId, exception.getMessage());
    }

    @Test
    void getListing_WhenListingNotFound_ThrowsIllegalArgumentException() {
        UUID listingId = UUID.randomUUID();
        HttpClientErrorException notFound = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8
        );

        when(restTemplate.getForObject(
                eq("http://catalogue-service:8081/api/v1/catalogue/listings/{listingId}/summary"),
                eq(ListingSummaryResponse.class),
                eq(listingId)
        )).thenThrow(notFound);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> catalogueServiceClient.getListing(listingId)
        );

        assertEquals("Listing not found: " + listingId, exception.getMessage());
    }
}
