//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONResponse
{
    private final boolean success;
    private final String error;

    public static JSONResponse success()
    {
        return new JSONResponse(true, null);
    }

    public static JSONResponse failure(final String error)
    {
        return new JSONResponse(false, error);
    }

    private JSONResponse(final boolean success,
                         final String error)
    {
        this.success = success;
        this.error = error;
    }

    public String toString()
    {
        return JSON.object().toJson(this);
    }

}

