package server.commands;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ClearCommand implements Command {
    @Override
    public String execute(ObjectInputStream ois, ObjectOutputStream oos) {
        // Сервер может просто вернуть специальный маркер, например: "__CLEAR__"
        return "__CLEAR__";
    }
}
