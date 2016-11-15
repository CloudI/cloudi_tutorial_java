//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:
package org.cloudi.examples.tutorial;

public class JSONItemRefreshOccurredNotification extends JSONResponse
{
    public static final String message_name_valid =
        "item_refresh_occurred";

    public static JSONItemRefreshOccurredNotification
        success()
    {
        return new JSONItemRefreshOccurredNotification(true, null);
    }

    public static JSONItemRefreshOccurredNotification
        failure(final String error)
    {
        return new JSONItemRefreshOccurredNotification(false, error);
    }

    private JSONItemRefreshOccurredNotification(final boolean success,
                                                final String error)
    {
        super(JSONItemRefreshOccurredNotification.message_name_valid,
              success, error);
    }

}

