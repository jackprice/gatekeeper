package io.gatekeeper.logging;

import java.util.logging.Logger;

public class Loggers {

    public static Logger getReplicationLogger() {
        return Logger.getLogger("replication");
    }

}
