package usi.justmove.utils;

/**
 * Created by usi on 13/12/16.
 */

public class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A a, B b) {
        first = a;
        second = b;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}
