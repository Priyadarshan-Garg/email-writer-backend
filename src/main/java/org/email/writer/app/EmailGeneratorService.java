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
/*
    it takes request from EmailGeneratorController and gives to EmailGeneratorService and service annotates that the core business logic lies here
    like data processing and API logic
    Analogy RestController is receptionist who gives you room and service is the manager which look after the hotel
 */
public class EmailGeneratorService {

    private final WebClient webClient;
    /*
       it is successor of RestTemplate it's reactive and non-blocking,
        non-blocking means it sends request but doesn't wait for the response
        two smart threads they sent request to gemini and start processing new request instead of waiting.
     */

    @Value("${gemini.api.url}")
    private String geminiAPIUrl;

    @Value("${gemini.api.key}")
    private String geminiAPIKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }
    /*
        Builder builds the webClient because it is private and protected so we use this and spring injects dependencies
     */

    /**
     *
     * @param emailRequest from user
     * @return JSON formatted response from AI
     */
    public String generateEmailReply(EmailRequest emailRequest) {
        // Although we already checked in Controller class but still for safety
        if (emailRequest == null || emailRequest.getEmailContent() == null || emailRequest.getEmailContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emailContent must not be empty");
        }
        // by any chance API key or API url is lost its system's fault so we need to tell the user too
        if (geminiAPIUrl == null || geminiAPIUrl.isBlank() || geminiAPIKey == null || geminiAPIKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Gemini API configuration is missing. Please set gemini.api.url and gemini.api.key");
        }

        // this our prompt which will be sent to Gemini models
        String prompt = buildPrompt(emailRequest);

        // This is the required data format for gemini endPoints to understand what is the metaData and data,it's strict so this the way we can send our prompt
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{   // represents history of conversation
                        Map.of("parts", new Object[]{   // represents a part of conversation like text, images etc
                                Map.of("text", prompt)  // represents the actual text or prompt
                        })
                }
        );
        /*
            uri is used to builds final Url instead of string concatenation we use this method
            everyone have same url but different keys and after that it returns as string
         */
        String uri = UriComponentsBuilder.fromHttpUrl(geminiAPIUrl)
                .queryParam("key", geminiAPIKey)
                .toUriString();

        try {
            String response = webClient.post() // saying webClient that it's a post request
                    .uri(uri) // here we set the address that where should you send this request(google servers)
                    .header("Content-Type", "application/json")  // telling it that data is in JSON format
                    .bodyValue(requestBody) // actual data
                    .retrieve() // request is ready and sent now wait for the response
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .map(body -> new ResponseStatusException(clientResponse.statusCode(),
                                            body == null || body.isBlank() ? "Gemini API error" : body))) // if it response with  40x or 5xx then treat it as error
                    .bodyToMono(String.class) // serve the requested response of user in string format
                    .block(); // it is used to block webClient until it gets response

            return extractResponseContent(response);  // extraction of response cuz it won't be plain text
        } catch (WebClientResponseException e) {
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    /**
     *
     * @param response which is also in JSON format as I said this is the way to communicate with AI models
     * @return extracts the string plain text from response
     */
    private String extractResponseContent(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();  // it's like a translator between java and JSON
            JsonNode rootNode = objectMapper.readTree(Optional.ofNullable(response).orElse("{}"));  // JsonNode is tree like structure makes java to understan

            JsonNode candidates = rootNode.path("candidates"); // AI thinks all possible solutions so it gives the response in candidates []
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode first = candidates.get(0); // First response of AI is highly relevant to the topic so we extract this

                JsonNode parts = first.path("content").path("parts"); // retrieval of data same as we sent
                if (!parts.isArray() || parts.isEmpty()) {
                    parts = first.path("contents").path("parts"); // AI gives different format of answers if first one fails then second attempt
                }
                if (parts.isArray() && !parts.isEmpty()) { // if Parts is array then we traverse it and extract its array
                    JsonNode text = parts.get(0).path("text");
                    if (!text.isMissingNode() && !text.isNull()) {
                        return text.asText(); // we found or required response
                    }
                }
            }
            return response; // nothing works then return response (fallback mechanism)
        } catch (Exception e) {
            return "Error Processing request: " + e.getMessage();
        }
    }

    /**
     *
     * @param emailRequest - >(content,tone)
     * @return exact prompt of given text according to the need of user and this prompt will be sent to AI models.
     */
    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        String instructions = emailRequest.getInstructions();

        // Check if instructions are provided, otherwise use a default
        if (instructions != null && !instructions.isBlank()) {
            prompt.append("You are given instructions follow them strictly no need to add extra things on your own just follow:").append(instructions);
        } else {
            // Updated default prompt: focused on generating content, not completing an email.
            prompt.append("You are a direct and concise AI assistant. Based on the following content, generate a response. Do not add any salutations, sign-offs, or extra sentences. Only provide the main response.");
        }

        if (emailRequest.getTone() != null && !emailRequest.getTone().isBlank()) {
            prompt.append(" Use a ").append(emailRequest.getTone()).append(" tone.");
        }

        prompt.append("\n\nOriginal Content:\n").append(emailRequest.getEmailContent());

        return prompt.toString();
    }
}
