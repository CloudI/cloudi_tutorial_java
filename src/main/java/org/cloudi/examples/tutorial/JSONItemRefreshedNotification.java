//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONItemRefreshedNotification extends JSONResponse
{
    public static final String message_name_valid = "item_refreshed";

    public static JSONItemRefreshedNotification success()
    {
        return new JSONItemRefreshedNotification(true, null);
    }

    public static JSONItemRefreshedNotification failure(final String error)
    {
        return new JSONItemRefreshedNotification(false, error);
    }

    private JSONItemRefreshedNotification(final boolean success,
                                          final String error)
    {
        super(JSONItemRefreshedNotification.message_name_valid,
              success, error);
    }

}

