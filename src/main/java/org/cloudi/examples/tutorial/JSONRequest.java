//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:
package org.cloudi.examples.tutorial;

public class JSONRequest
{
    private String message_name;

    public static JSONRequest fromString(final String json)
    {
        return JSON.object()
                   .fromJson(json, JSONRequest.class);
    }

    protected static <T extends JSONRequest> T fromString(String json,
                                                          Class<T> type)
    {
        final T json_request = JSON.object()
                                   .fromJson(json, type);
        if (! json_request.valid())
            return null;
        return json_request;
    }

    public boolean valid()
    {
        return this.message_name != null;
    }

    public String getMessageName()
    {
        return this.message_name;
    }

}

