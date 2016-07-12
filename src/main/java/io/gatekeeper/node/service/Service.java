package io.gatekeeper.node.service;

import java.util.concurrent.CompletableFuture;

public interface Service extends AutoCloseable {

    CompletableFuture start();
}
