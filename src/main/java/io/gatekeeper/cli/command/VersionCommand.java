package io.gatekeeper.cli.command;

import io.gatekeeper.Version;
import io.gatekeeper.cli.AbstractCommand;
import io.gatekeeper.cli.Input;
import io.gatekeeper.cli.Output;

public class VersionCommand extends AbstractCommand {


    @Override
    public void run(Input input, Output output) {
        output.println(Version.CURRENT.toString());
    }

    @Override
    public void configure() {
        this.setName("version");
        this.setDescription("prints the current gatekeeper version");
    }
}
