package console.commands;

import george.resident.sync.ConnectivityResident;

/**
 * The abstract class extended by individual commands: Device, Exit, Sync.
 * It provides the run function which will be implemented
 * by every command such that said command will execute accordingly.
 */
public abstract class Command {
    protected ConnectivityResident app;

    public void setApp(ConnectivityResident app) {
        this.app = app;
    }

    public abstract void run(String[] arguments) throws Exception;

    //TODO: perhaps a better way to do it...
    public abstract Exception manual();
}
