package console.commands;

import george.resident.exceptions.DeviceNotFound;

import java.io.IOException;
import java.nio.file.Path;

public class SyncCommand extends ActionCommand {
    public SyncCommand() {
        super("sync");
    }

    @Override
    protected void action(int index, Path path) throws DeviceNotFound, IOException {
        app.syncFiles(index, path);
    }
}
