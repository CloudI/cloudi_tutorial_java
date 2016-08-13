//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONLanguageListRequest extends JSONRequest
{
    public static final String message_name_valid = "language_list";

    public static JSONLanguageListRequest fromString(final String json)
    {
        return JSONRequest.fromString(json, JSONLanguageListRequest.class);
    }

    public boolean valid()
    {
        return this.getMessageName() ==
               JSONLanguageListRequest.message_name_valid;
    }

}

