package com.richard.routeguide;

import com.google.protobuf.util.JsonFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created on 5/24/2017.
 */
public class RouteGuideUtil {
    private static final double COORD_FACTOR = 1e7;

    /**
     * Gets the latitude for the given point.
     */
    public static double getLatitude(Point location) {
        return location.getLatitude() / COORD_FACTOR;
    }

    /**
     * Gets the longitude for the given point.
     */
    public static double getLongitude(Point location) {
        return location.getLongitude() / COORD_FACTOR;
    }

    /**
     * Gets the default features file from classpath.
     *//*
    public static URL getDefaultFeaturesFile() {
        return Application.class.getClassLoader().getResource("classpath:route_guide_db.json");
    }*/

    public static String getDefaultFeatures(){
        return readMessageFromFileThroughClasspath(RouteGuideClient.class, "route_guide_db.json");
    }

    /**
     * Parses the JSON input file containing the list of features.
     */
    /*public static List<Feature> parseFeatures(URL file) throws IOException {
        try (InputStream input = file.openStream()) {
            try (Reader reader = new InputStreamReader(input)) {
                FeatureDatabase.Builder database = FeatureDatabase.newBuilder();
                JsonFormat.parser().merge(reader, database);
                return database.getFeatureList();
            }
        }
    }*/

    /**
     * Parses the JSON input file containing the list of features.
     */
    public static List<Feature> parseFeatures(String jsonFile) throws IOException {
        FeatureDatabase.Builder database = FeatureDatabase.newBuilder();
        JsonFormat.parser().merge(jsonFile, database);
        return database.getFeatureList();
    }

    /**
     * Indicates whether the given feature exists (i.e. has a valid name).
     */
    public static boolean exists(Feature feature) {
        return feature != null && !feature.getName().isEmpty();
    }

    public static String readMessageFromFileThroughClasspath(Class<?> clzz, String fileName) {
        StringBuilder builder = new StringBuilder();

        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(
                           clzz.getClassLoader().getResourceAsStream(fileName), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null)
                builder.append(line);

            return builder.toString();
            //return XmlUtil.linearizeXml(builder.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
