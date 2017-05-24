package com.richard.routeguide;

/**
 * Created on 5/24/2017.
 */
public class Application {
    /**
     * Main method.  This comment makes the linter happy.
     */
    public static void main(String[] args) throws Exception {
        RouteGuideServer server = new RouteGuideServer(6565);
        server.start();
        server.blockUntilShutdown();

        /*String applicationDb = RouteGuideUtil.readMessageFromFileThroughClasspath(Application.class, "route_guide_db.json");
        System.out.println(applicationDb);*/
    }
}
