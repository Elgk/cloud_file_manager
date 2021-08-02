package model;

public class DeleteRequest extends AbstractCommand{
    private String  fileName;

    public DeleteRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_DELETE;
    }
}
