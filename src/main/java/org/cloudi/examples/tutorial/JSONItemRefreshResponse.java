//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.util.List;
import java.util.LinkedList;

public class JSONItemRefreshResponse extends JSONResponse
{
    public static JSONItemRefreshResponse success()
    {
        return new JSONItemRefreshResponse(true, null);
    }

    public static JSONItemRefreshResponse failure(final String error)
    {
        return new JSONItemRefreshResponse(false, error);
    }

    private JSONItemRefreshResponse(final boolean success,
                                    final String error)
    {
        super(JSONItemRefreshRequest.message_name_valid,
              success, error);
    }

}

