//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONItemRefreshRequest extends JSONRequest
{
    public static final String message_name_valid = "item_refresh";

    public static JSONItemRefreshRequest fromString(final String json)
    {
        return JSONRequest.fromString(json, JSONItemRefreshRequest.class);
    }

    public boolean valid()
    {
        return this.getMessageName().equals(this.message_name_valid);
    }

}

