package org.email.writer.app;

import lombok.Data;
/*
    Pojo class for modifying the data
 */
@Data // Lombok annotation for generating setter getters toString and many more...

public class EmailRequest {
    // Ensures the content of email if it's empty then it will show user BAD request
    private String emailContent;
    // to convert the tone as per user's choice
    private String tone;
    // to give instructions to the Model how we want this email to be written
    private String instructions;
}
