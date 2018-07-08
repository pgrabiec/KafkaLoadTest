package pl.edu.agh.kafkaload.util;

public class NumbersConverter {
    private NumbersConverter() {
    }

    public static Double convert(Object number) {
        if (number instanceof Integer) {
            return (double) (int) number;

        } else if (number instanceof Long) {
            return (double) (long) number;

        } else if (number instanceof Double) {
            return (double) number;

        } else {
            return null;
        }
    }
}
