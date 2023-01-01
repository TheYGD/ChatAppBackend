package pl.szmidla.chatappbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
@ResponseBody
public class ItemAlreadyExistsException extends RuntimeException {
        public static String MESSAGE_TEMPLATE = "%s already exists!";

        public ItemAlreadyExistsException() {
            super(String.format(MESSAGE_TEMPLATE,  "Item"));
        }
        public ItemAlreadyExistsException(String itemName) {
            super(String.format(MESSAGE_TEMPLATE, itemName.substring(0, 1).toUpperCase() + itemName.substring(1 )));
        }
    }

