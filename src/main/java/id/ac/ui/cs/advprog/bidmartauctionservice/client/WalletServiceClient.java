package id.ac.ui.cs.advprog.bidmartauctionservice.client;

import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.HoldFundsRequest;
import id.ac.ui.cs.advprog.bidmartauctionservice.dto.wallet.ReleaseFundsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class WalletServiceClient {

    private final RestTemplate restTemplate;

    @Value("${wallet.service.url:http://localhost:8082}")
    private String walletServiceUrl;

    public void holdFunds(HoldFundsRequest request) {
        String url = walletServiceUrl + "/api/v1/wallet/hold";
        restTemplate.postForObject(url, request, Void.class);
    }

    public void releaseFunds(ReleaseFundsRequest request) {
        String url = walletServiceUrl + "/api/v1/wallet/release";
        restTemplate.postForObject(url, request, Void.class);
    }
}
