package me.drblau.fumailapp.util.common;

import java.util.concurrent.atomic.AtomicInteger;

public class UNID {
    private final static AtomicInteger c =  new AtomicInteger(0);

    public static int generateID() {
        return c.getAndIncrement();
    }
}
