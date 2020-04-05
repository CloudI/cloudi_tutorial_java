//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:

package org.cloudi.examples.tutorial;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.LinkedList;
import org.cloudi.API;

public class ServiceIdle
{
    public static final int INTERVAL = 1000; // milliseconds
    public static final int SIZE_MAX = 1000;
    public static final int SIZE_CHUNK = SIZE_MAX / 2;

    public static interface Callable
    {
        public void call(final API api);
    }

    public static class Queue
    {
        private final ArrayBlockingQueue<Callable> queue;

        public Queue()
        {
            this.queue = new ArrayBlockingQueue<Callable>(ServiceIdle.SIZE_MAX);
        }

        public void in(final Callable o)
        {
            try
            {
                this.queue.put(o);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace(Main.err);
            }
        }

        public LinkedList<Callable> out()
        {
            final LinkedList<Callable> out = new LinkedList<Callable>();
            this.queue.drainTo(out, ServiceIdle.SIZE_CHUNK);
            return out;
        }
    }

    private final API api;
    private final Queue queue;

    public ServiceIdle(final API api)
    {
        this.api = api;
        this.queue = new Queue();
    }

    public void check()
    {
        final LinkedList<Callable> idle = this.queue.out();
        while (! idle.isEmpty())
        {
            final Callable o = idle.removeFirst();
            o.call(this.api);
        }
    }

    public void execute(final Callable o)
    {
        this.queue.in(o);
    }

}

