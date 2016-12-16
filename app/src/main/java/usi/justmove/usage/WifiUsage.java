package usi.justmove.usage;

/**
 * Created by usi on 16/12/16.
 */

public class WifiUsage {
    private double txUsage;
    private double rxUsage;

    public WifiUsage(double txUsage, double rxUsage) {
        this.rxUsage = rxUsage;
        this.txUsage = txUsage;
    }

    public double getRxUsage() {
        return rxUsage;
    }

    public double getTxUsage() {
        return txUsage;
    }
}
