package model;

public class AuthNoResponse extends AbstractCommand{
    private String message;

    public AuthNoResponse(String message){
     this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_NO;
    }
}
