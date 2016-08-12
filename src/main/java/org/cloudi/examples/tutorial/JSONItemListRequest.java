//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONItemListRequest
{
    private long user_id;

    public static JSONItemListRequest fromString(final String json)
    {
        return JSON.object()
                   .fromJson(json, JSONItemListRequest.class);
    }

    public long getUserId()
    {
        return this.user_id;
    }

}

