package com.impactsoftware.bookutilities;


import org.cloudi.API;

public class Main {
    public static void main(String[] args) {
        try {
            final int thread_count = API.thread_count();
            assert (thread_count == 1);
            Task t = new Task(0);
            t.run();
        } catch (API.InvalidInputException e) {
            e.printStackTrace(API.err);
        }
    }
}
