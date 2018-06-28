package pl.edu.agh.kafkaload.kafkametrics;


public class MetricsParam {


    private int time;
    private int m1;
    private int m2;
    private int m3;

    public MetricsParam(int time,int m1, int m2, int m3) {
        this.time = time;
        this.m1 = m1;
        this.m2 = m2;
        this.m3 = m3;
    }

    public MetricsParam(int time){
        this.time = time;
    }


    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getMetric1() {
        return m1;
    }

    public void setMetric1(int m1) {
        this.m1 = m1;
    }
    public int getMetric2() {
        return m2;
    }

    public void setMetric2(int m2) {
        this.m2 = m2;
    }
    public int getMetric3() {
        return m3;
    }

    public void setMetric3(int m3) {
        this.m3 = m3;
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("Metric{time=").append(time).append(", m1=")
                .append(m1).append(", m2=").append(m2).append(", m3=").append(m3).append("}");

        return builder.toString();
    }



}