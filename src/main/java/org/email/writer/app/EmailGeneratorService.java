package org.email.writer.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.Optional;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiAPIUrl;

    @Value("${gemini.api.key}")
    private String geminiAPIKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        if (emailRequest == null || emailRequest.getEmailContent() == null || emailRequest.getEmailContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emailContent must not be empty");
        }
        if (geminiAPIUrl == null || geminiAPIUrl.isBlank() || geminiAPIKey == null || geminiAPIKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini API configuration is missing. Please set gemini.api.url and gemini.api.key");
        }

        String prompt = buildPrompt(emailRequest);

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        String uri = UriComponentsBuilder.fromHttpUrl(geminiAPIUrl)
                .queryParam("key", geminiAPIKey)
                .toUriString();

        try {
            String response = webClient.post()
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> new ResponseStatusException(clientResponse.statusCode(),
                                            body == null || body.isBlank() ? "Gemini API error" : body)))
                    .bodyToMono(String.class)
                    .block();

            return extractResponseContent(response);
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    private String extractResponseContent(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(Optional.ofNullable(response).orElse("{}"));

            // Try the common schema: candidates[0].content.parts[0].text
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode first = candidates.get(0);
                // First try singular 'content'
                JsonNode parts = first.path("content").path("parts");
                if (!parts.isArray() || parts.size() == 0) {
                    // Fallback to plural 'contents' then parts
                    parts = first.path("contents").path("parts");
                }
                if (parts.isArray() && parts.size() > 0) {
                    JsonNode text = parts.get(0).path("text");
                    if (!text.isMissingNode() && !text.isNull()) {
                        return text.asText();
                    }
                }
            }
            // If parsing fails, return raw response for debugging
            return response;
        } catch (Exception e) {
            return "Error Processing request: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. Please don't generate subject line ");
        if (emailRequest.getTone() != null && emailRequest.getEmailContent() != null && !emailRequest.getEmailContent().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append("\n Original email: \n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}
