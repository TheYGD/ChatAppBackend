package pl.szmidla.chatappbackend.data.other;

import lombok.Getter;

@Getter
public class UserActivationToken {
    static final char ID_TOKEN_DIVIDER_CHAR = '_';
    private final long id;
    private final String token;

    private UserActivationToken(long id, String token) {
        this.id = id;
        this.token = token;
    }

    public static UserActivationToken create(String idAndToken) {
        int dividerPos = idAndToken.indexOf(ID_TOKEN_DIVIDER_CHAR);
        long id = Long.parseLong(idAndToken.substring( 0, dividerPos ));
        String token = idAndToken.substring( dividerPos + 1 );

        return new UserActivationToken(id, token);
    }
}
