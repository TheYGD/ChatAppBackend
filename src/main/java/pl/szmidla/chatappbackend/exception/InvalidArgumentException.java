package pl.szmidla.chatappbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidArgumentException extends RuntimeException {
    public static String MESSAGE_TEMPLATE = "Action already performed. %s";

    public InvalidArgumentException() {
        super(String.format(MESSAGE_TEMPLATE,  ""));
    }
    public InvalidArgumentException(String itemName) {
        super(String.format(MESSAGE_TEMPLATE,
                "( " + itemName.substring(0, 1).toUpperCase() + itemName.substring(1 ) + " )"));
    }
}
