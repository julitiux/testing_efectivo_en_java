package com.circulosiete.curso.minibank.payments.adapter.spei;


import com.circulosiete.curso.minibank.payments.ports.out.PaymentRailPort;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Adapter HTTP hacia un gateway SPEI (esquemático).
 */
public class SpeiPaymentRailHttpAdapter implements PaymentRailPort {

    private final String baseUrl;
    private final RestTemplate http;

    public SpeiPaymentRailHttpAdapter(String baseUrl, RestTemplate http) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = http;
    }

    @Override
    public TransferResponse send(TransferRequest req) {
        var url = baseUrl + "/payments/credit-transfer";
        var body = Map.of(
            "requestId", req.requestId(),
            "debtorAccount", req.debtorAccount(),
            "creditorAccount", req.creditorAccount(),
            "creditorBankId", req.creditorBankId(),
            "currency", req.currency(),
            "amount", req.amount(),
            "purpose", req.purpose()
        );
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>(body, headers);

        var resp = http.exchange(url, HttpMethod.POST, entity, Map.class);
        var m = resp.getBody();
        var externalRef = m != null ? String.valueOf(m.get("externalRef")) : null;
        var accepted = m != null && Boolean.TRUE.equals(m.get("accepted"));
        var message = m != null ? String.valueOf(m.getOrDefault("message", "")) : "";
        return new TransferResponse(externalRef, accepted, message);
    }

    @Override
    public StatusResponse status(String externalRef) {
        var url = baseUrl + "/payments/status/" + externalRef;
        var resp = http.getForEntity(url, Map.class);
        var m = resp.getBody();
        var status = m != null ? String.valueOf(m.getOrDefault("status", "UNKNOWN")) : "UNKNOWN";
        return new StatusResponse(externalRef, status, m != null ? m.toString() : "{}");
    }

    @Override
    public boolean cancel(String externalRef) {
        var url = baseUrl + "/payments/" + externalRef;
        http.delete(url);
        return true;
    }
}

