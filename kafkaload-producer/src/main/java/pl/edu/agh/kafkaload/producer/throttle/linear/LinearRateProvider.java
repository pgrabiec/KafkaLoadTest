package pl.edu.agh.kafkaload.producer.throttle.linear;

import pl.edu.agh.kafkaload.producer.throttle.RateProvider;

import java.util.*;

public class LinearRateProvider implements RateProvider {
    private final int messageSize;
    private final Iterator<LoadPoint> iterator;
    private LoadPoint left, right;
    private double a, b;

    public LinearRateProvider(Collection<LoadPoint> points, int messageSize) {
        this.messageSize = messageSize;
        List<LoadPoint> pointsList = new LinkedList<>(points);
        pointsList.sort(Comparator.comparingDouble(LoadPoint::getX));
        this.iterator = pointsList.iterator();
        if (pointsList.size() < 2) {
            throw new IllegalArgumentException(
                    "Linear rate provider needs at least 2 points, but " + pointsList.size() + " were passed"
            );
        }
        this.left = iterator.next();
        this.right = iterator.next();
        updateCurve();
    }

    private void updateCurve() {
        double x1 = left.getX();
        double x2 = right.getX();
        double y1 = left.getY();
        double y2 = right.getY();
        if (x1 == x2) {
            a = 0;
            b = y2;
            return;
        }

        this.a = (y2 - y1) / (x2 - x1);
        this.b = y1 - x1 * a;
    }

    private boolean moveRight() {
        if (right == null) {
            return false;
        }

        if (!iterator.hasNext()) {
            left = right;
            right = null;
            return false;
        }

        left = right;
        right = iterator.next();
        updateCurve();
        return true;
    }

    @Override
    public double messageRateAt(long offset) {
        if (offset < left.getX()) {
            return 0.0;
        }
        if (offset > right.getX()) {
            if (!moveRight()) {
                return 0.0;
            }
        }

        return a * offset + b;
    }

    @Override
    public boolean finished(long offset) {
        if (offset < right.getX()) {
            return false;
        }

        return !moveRight();
    }

    @Override
    public int messageSize() {
        return this.messageSize;
    }
}
