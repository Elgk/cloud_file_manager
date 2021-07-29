package model;

public class PathUpResponse extends AbstractCommand{
    private String path;

    public PathUpResponse(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public CommandType getType() {
        return CommandType.PATH_RESPONSE;
    }
}
