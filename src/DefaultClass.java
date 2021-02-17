public class DefaultClass {
    public double count;
    public String nextHop;

    // Default Values
    public DefaultClass() {
        this.count = Double.POSITIVE_INFINITY;
        this.nextHop = null;
    }

    // Values passed by the users
    public DefaultClass(double count, String nextHop) {
        this.count = count;
        this.nextHop = nextHop;
    }
}
