package pl.edu.agh.kafkaload.producer.throttle.linear;

import pl.edu.agh.kafkaload.producer.throttle.RateProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LinearRateFactory {
    public List<RateProvider> createProviders(Reader reader) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(reader);

        if (!bufferedReader.ready()) {
            return Collections.emptyList();
        }
        String firstLine = bufferedReader.readLine().trim();
        int replicas = Integer.parseInt(firstLine);

        String secondLine = bufferedReader.readLine().trim();
        int messageSize = Integer.parseInt(secondLine);

        List<LoadPoint> points = new LinkedList<>();
        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            line = line.trim();
            String[] values = line.split(" ");
            if (values.length != 2) {
                System.out.println("Invalid columns number in line: " + line);
                continue;
            }
            long x = Long.parseLong(values[0]);
            double y = Double.parseDouble(values[1]);

            points.add(new LoadPoint(x * 1000000, y));
        }

        List<LoadPoint> sharedLoadPoints = points.stream()
                .map(point -> new LoadPoint(
                        point.getX(),
                        point.getY() / replicas)
                )
                .collect(Collectors.toList());

        List<RateProvider> providers = new ArrayList<>(replicas);
        for (int i = 0; i < replicas; i++) {
            providers.add(new LinearRateProvider(sharedLoadPoints, messageSize));
        }

        return providers;
    }
}
