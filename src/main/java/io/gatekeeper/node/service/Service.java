package io.gatekeeper.node.service;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

public interface Service extends Closeable {

    CompletableFuture start();

}
