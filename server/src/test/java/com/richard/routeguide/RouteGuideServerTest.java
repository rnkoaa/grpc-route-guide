package com.richard.routeguide;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
/*import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;*/

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by 7/10/17.
 */
public class RouteGuideServerTest {
    private RouteGuideServer server;
    private ManagedChannel inProcessChannel;
    private Collection<Feature> features;


    @Before
    public void setUp() throws Exception {
        String uniqueServerName = "in-process server for " + getClass();
        features = new ArrayList<Feature>();
        // use directExecutor for both InProcessServerBuilder and InProcessChannelBuilder can reduce the
        // usage timeouts and latches in test. But we still add timeout and latches where they would be
        // needed if no directExecutor were used, just for demo purpose.
        server = new RouteGuideServer(
                InProcessServerBuilder.forName(uniqueServerName).directExecutor(), 0, features);
        server.start();
        inProcessChannel = InProcessChannelBuilder.forName(uniqueServerName).directExecutor().build();
    }

    @After
    public void tearDown() throws Exception {
        inProcessChannel.shutdownNow();
        server.stop();
    }

    @Test
    public void test_that_server_is_not_null() {
        assertNotNull(server);
    }

    @Test
    public void get_feature_success() {
        Point point = Point.newBuilder().setLongitude(1).setLatitude(1).build();
        Feature unnamedFeature = Feature.newBuilder()
                .setName("").setLocation(point).build();

        RouteGuideGrpc.RouteGuideBlockingStub stub = RouteGuideGrpc.newBlockingStub(inProcessChannel);

        // feature not found in the server
        Feature feature = stub.getFeature(point);

        assertEquals(unnamedFeature, feature);

        // feature found in the server
        Feature namedFeature = Feature.newBuilder()
                .setName("name").setLocation(point).build();
        features.add(namedFeature);

        feature = stub.getFeature(point);

        assertEquals(namedFeature, feature);
    }

    @Test
    public void listFeatures() throws Exception {
        // setup
        Rectangle rect = Rectangle.newBuilder()
                .setLo(Point.newBuilder().setLongitude(0).setLatitude(0).build())
                .setHi(Point.newBuilder().setLongitude(10).setLatitude(10).build())
                .build();
        Feature f1 = Feature.newBuilder()
                .setLocation(Point.newBuilder().setLongitude(-1).setLatitude(-1).build())
                .setName("f1")
                .build(); // not inside rect
        Feature f2 = Feature.newBuilder()
                .setLocation(Point.newBuilder().setLongitude(2).setLatitude(2).build())
                .setName("f2")
                .build();
        Feature f3 = Feature.newBuilder()
                .setLocation(Point.newBuilder().setLongitude(3).setLatitude(3).build())
                .setName("f3")
                .build();
        Feature f4 = Feature.newBuilder()
                .setLocation(Point.newBuilder().setLongitude(4).setLatitude(4).build())
                .build(); // unamed
        features.add(f1);
        features.add(f2);
        features.add(f3);
        features.add(f4);
        final Collection<Feature> result = new HashSet<Feature>();
        final CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<Feature> responseObserver =
                new StreamObserver<Feature>() {
                    @Override
                    public void onNext(Feature value) {
                        result.add(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                };
        RouteGuideGrpc.RouteGuideStub stub = RouteGuideGrpc.newStub(inProcessChannel);

        // run
        stub.listFeatures(rect, responseObserver);
        assertTrue(latch.await(1, TimeUnit.SECONDS));

        // verify
        assertEquals(new HashSet<Feature>(Arrays.asList(f2, f3)), result);
    }

}
