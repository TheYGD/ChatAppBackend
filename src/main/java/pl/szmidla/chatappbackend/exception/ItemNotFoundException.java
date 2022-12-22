package pl.szmidla.chatappbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
@ResponseBody
public class ItemNotFoundException extends RuntimeException {
    public static String MESSAGE_TEMPLATE = "%s not found!";

    public ItemNotFoundException() {
        super(String.format(MESSAGE_TEMPLATE,  "Item"));
    }
    public ItemNotFoundException(String itemName) {
        super(String.format(MESSAGE_TEMPLATE, itemName.substring(0, 1).toUpperCase() + itemName.substring(1 )));
    }
}
