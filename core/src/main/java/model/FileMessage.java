package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractCommand{
    private final String  name;
    private final Long size;
    private final byte[] data;

    public FileMessage(Path path) throws IOException {
        name = path.getFileName().toString();
        size = Files.size(path);
        data = Files.readAllBytes(path);

    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public CommandType getType() {
        return CommandType.FILE_MESSAGE;
    }
}
