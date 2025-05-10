package server.handlers;

import server.commands.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CommandHandler {
    private final Map<String, Command> commands;

    public CommandHandler() {
        commands = new HashMap<>();
        commands.put("put", new PutCommand());
        commands.put("get", new GetCommand());
        commands.put("ls", new LsCommand());
        commands.put("pwd", new PwdCommand());
        commands.put("cd", new CdCommand());
        commands.put("file", new FileCommand());
        commands.put("clear", new ClearCommand());

    }

    public String handleCommand(String commandName, ObjectInputStream ois, ObjectOutputStream oos) {
        Command command = commands.get(commandName.toLowerCase());
        if (command != null) {
            return command.execute(ois, oos);
        }
        return "Unknown command: " + commandName;
    }
}