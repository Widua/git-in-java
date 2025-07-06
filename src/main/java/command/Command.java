package command;

import java.util.Map;

public interface Command {
    void execute(Map<String,String> options);
}
