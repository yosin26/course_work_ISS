package server.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface Command {
    String execute(ObjectInputStream ois, ObjectOutputStream oos);
}