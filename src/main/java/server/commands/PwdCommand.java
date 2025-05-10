package server.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PwdCommand implements Command {
    @Override
    public String execute(ObjectInputStream ois, ObjectOutputStream oos) {
        return System.getProperty("user.dir");
    }
}