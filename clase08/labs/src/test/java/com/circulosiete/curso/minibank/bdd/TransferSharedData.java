package com.circulosiete.curso.minibank.bdd;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Estado compartido entre pasos (World)
 *
 */
@Component
@ScenarioScope
public class TransferSharedData {
    public final Map<String, UUID> idsByName = new HashMap<>();
    public Response lastResponse;
    public Response secondResponse;
    public String lastIdempotencyKey;
    public Map<String, Object> lastBody;
}
