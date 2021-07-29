package model;

import java.nio.file.Path;

public class FileRequest extends AbstractCommand{
    private final String name;

    public FileRequest(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public CommandType getType(){
        return CommandType.FILE_REQUEST;
    }
}
