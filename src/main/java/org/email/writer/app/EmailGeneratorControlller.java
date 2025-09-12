package org.email.writer.app;

import lombok.AllArgsConstructor; // To reduce boilerplate code by injecting getter and setter at runtime
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
/* It serves data(like JSON) while controller serves entire web page where
 we get redirected,but it just gives the list or data
 */

@RequestMapping("api/email/")
/* API Endpoints are specific urls where you can send and receive data
    and once the user hit the url in braces it starts generateEmail method
 */
@AllArgsConstructor // for generating all argument constructor for class

@CrossOrigin(origins = "*")
/*
    Cross Origin Resources Sharing
    when you see a X or YT video embedded in news article they both are on different domain but still we see on same web and domain. Usually browser doesn't allow but this let
    us do
 */
public class EmailGeneratorControlller {
    private final EmailGeneratorService emailGeneratorService;

    @PostMapping("generate")

    /**
     * ResponseEntity gives full access to HTTP response it doesn't only @return actual data but also status code and header(metaData of information)
        * if it was returning String it was always 200 OK but when it's issue from users end it will respond with badRequest or other ones.
     * if it is null or empty it throws bad request otherwise it call generateEmailReply and stores in response and
     * send back the  response to the user
     */
    public ResponseEntity<Map<String, Object>> generateEmail(@RequestBody EmailRequest emailRequest) throws ResponseStatusException {
        /*
        RequestBody accepts request from user
         */

        if ((emailRequest == null) ||
                ((emailRequest.getEmailContent() == null || emailRequest.getEmailContent().isBlank())
                        && (emailRequest.getInstructions() == null || emailRequest.getInstructions().isBlank()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either emailContent or instruction must not be empty");
        }
        String response = emailGeneratorService.generateEmailReply(emailRequest);

        // wrapping response in a standard JSON format so frontend can reliably parse it
        Map<String, Object> body = new LinkedHashMap<>(); // preserves insertion order in JSON
        body.put("success", Boolean.TRUE);
        body.put("data", Map.of(
                "text", response
        ));
        return ResponseEntity.ok(body);
    }
}
