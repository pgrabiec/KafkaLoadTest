package pl.edu.agh.kafkaload.kafkametrics.providers;

public enum Unit {
    UNIT(1),
    HALF_KILO(500),

    BYTE(1),
    MEGABYTE(1024 * 1024),

    MILLISECOND(1),
    DECY_SECOND(100), // 1/10 s
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
