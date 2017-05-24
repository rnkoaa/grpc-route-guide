package com.richard.routeguide;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created on 5/24/2017.
 */
public class RouteGuideServer {
    private static final Logger logger = Logger.getLogger(RouteGuideServer.class.getName());

    private final int port;
    private final Server server;

    RouteGuideServer(int port) throws IOException {
        //this(port, RouteGuideUtil.getDefaultFeatures());
        this(ServerBuilder.forPort(port), port, RouteGuideUtil.parseFeatures(RouteGuideUtil.getDefaultFeatures()));
    }

    /**
     * Create a RouteGuide server listening on {@code port} using {@code featureFile} database.
     */
    /*private RouteGuideServer(int port, String defaultFeatures) throws IOException {
        this(ServerBuilder.forPort(port), port, RouteGuideUtil.parseFeatures(defaultFeatures));
    }*/

    /**
     * Create a RouteGuide server using serverBuilder as a base and features as data.
     */
    private RouteGuideServer(ServerBuilder<?> serverBuilder, int port, Collection<Feature> features) {
        this.port = port;
        server = serverBuilder.addService(new RouteGuideService(features))
                .build();
    }

    /**
     * Start serving requests.
     */
    void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may has been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                RouteGuideServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    /**
     * Stop serving requests and shutdown resources.
     */
    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


}
