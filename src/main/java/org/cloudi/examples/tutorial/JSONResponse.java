//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONResponse
{
    private String message_name;
    private boolean success;
    private String error;

    public static JSONResponse failure(final String error)
    {
        return new JSONResponse(null, false, error);
    }

    protected JSONResponse(final String message_name,
                           final boolean success,
                           final String error)
    {
        this.message_name = message_name;
        this.success = success;
        this.error = error;
    }

    public String toString()
    {
        return JSON.object().toJson(this);
    }

    public boolean getSuccess()
    {
        return this.success;
    }

}

