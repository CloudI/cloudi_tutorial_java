//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import org.cloudi.API;

public class GutenbergRefreshDone implements ServiceIdle.Callable
{
    private final boolean success;
    private final String error;

    public static GutenbergRefreshDone success()
    {
        return new GutenbergRefreshDone(true, null);
    }

    public static GutenbergRefreshDone failure(final String error)
    {
        return new GutenbergRefreshDone(false, error);
    }

    private GutenbergRefreshDone(final boolean success,
                                 final String error)
    {
        this.success = success;
        this.error = error;
    }

    public void call(final API api)
    {
        byte[] notification;
        if (this.success)
        {
            notification = JSONItemRefreshOccurredNotification
                .success().toString().getBytes();
        }
        else
        {
            notification = JSONItemRefreshOccurredNotification
                .failure(this.error).toString().getBytes();
        }
        final String name_websockets = api.prefix() + "client/websocket";
        try
        {
            api.mcast_async(name_websockets,
                            notification);
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
        }
    }
}

