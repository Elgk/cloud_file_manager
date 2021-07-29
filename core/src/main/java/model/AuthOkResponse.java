package model;

public class AuthOkResponse extends AbstractCommand{
    private String nickname;

    public AuthOkResponse(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_OK;
    }
}
