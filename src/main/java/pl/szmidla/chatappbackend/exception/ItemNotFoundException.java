package pl.szmidla.chatappbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ItemNotFoundException extends RuntimeException {

    private String itemName = "item";
    public static String MESSAGE_TEMPLATE = "%s not found!";

    public ItemNotFoundException() {}
    public ItemNotFoundException(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String toString() {
        String capitalizedName = itemName.substring(0, 1).toUpperCase() + itemName.substring(1);
        return MESSAGE_TEMPLATE.formatted(capitalizedName);
    }
}
