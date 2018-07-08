package pl.edu.agh.kafkaload.kafkametrics.providers;

public enum Unit {
    UNIT(1),

    BYTE(1),
    MEGABYTE(1024 * 1024),
    GIGABYTE(1024 * 1023 * 1024),

    MILLISECOND(1),
    SECOND(1000);

    private final long value;

    Unit(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public static double getScaleFactor(Unit sourceUnit, Unit targetUnit) {
        return (double) sourceUnit.getValue() / (double) targetUnit.getValue();
    }
}
