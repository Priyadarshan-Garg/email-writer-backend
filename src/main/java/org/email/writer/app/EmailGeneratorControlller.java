package org.email.writer.app;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("api/email/")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class EmailGeneratorControlller {
    private final EmailGeneratorService emailGeneratorService;

    @PostMapping("generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest) throws ResponseStatusException {
        if (emailRequest == null || emailRequest.getEmailContent() == null || emailRequest.getEmailContent().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "emailContent must not be empty");
        }
        String response = emailGeneratorService.generateEmailReply(emailRequest);
        return ResponseEntity.ok(response);
    }
}
