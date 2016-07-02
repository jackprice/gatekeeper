package io.gatekeeper.logging;

import java.util.logging.Logger;

public class Loggers {

    public static Logger getNodeLogger() {
        return Logger.getLogger("node");
    }

    public static Logger getReplicationLogger() {
        return Logger.getLogger("replication");
    }

    public static Logger getBackendLogger() {
        return Logger.getLogger("backend");
    }

    public static Logger getApiLogger() {
        return Logger.getLogger("api");
    }

}
