//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:

package org.cloudi.examples.tutorial;

import java.io.PrintStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import com.beust.jcommander.JCommander;
import org.cloudi.API;

public class Main
{
    public static final PrintStream out = API.out;
    public static final PrintStream err = API.err;

    private static Arguments arguments_parsed;
    public static Arguments arguments()
    {
        return Main.arguments_parsed;
    }

    public static void main(String[] args_in)
    {
        try
        {
            Arguments args_out = new Arguments();
            new JCommander(args_out, args_in);
            Main.arguments_parsed = args_out;

            final int thread_count = API.thread_count();
            ExecutorService threads =
                Executors.newFixedThreadPool(thread_count);
            for (int thread_index = 0;
                 thread_index < thread_count;
                 ++thread_index)
            {
                threads.execute(new Task(thread_index));
            }
            threads.shutdown();
        }
        catch (API.InvalidInputException e)
        {
            e.printStackTrace(Main.err);
        }
    }

    public static void info(final Object instance,
                            final String message)
    {
        Main.log(Main.out, instance, message);
    }

    public static void info(final Object instance,
                            final String format,
                            final Object... args)
    {
        Main.log(Main.out, instance, format, args);
    }

    public static void error(final Object instance,
                             final String message)
    {
        Main.log(Main.err, instance, message);
    }

    public static void error(final Object instance,
                             final String format,
                             final Object... args)
    {
        Main.log(Main.err, instance, format, args);
    }

    private static void log(final PrintStream destination,
                            final Object instance,
                            final String message)
    {
        destination.println(instance.getClass().getName() + " " + message);
    }

    private static void log(final PrintStream destination,
                            final Object instance,
                            final String format,
                            final Object... args)
    {
        destination.format(instance.getClass().getName() + " " + format, args);
    }

}

