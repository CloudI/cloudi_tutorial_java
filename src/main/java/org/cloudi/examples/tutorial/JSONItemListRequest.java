//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONItemListRequest extends JSONRequest
{
    public static final String message_name_valid = "item_list";
    private long user_id;
    private String language;

    public static JSONItemListRequest fromString(final String json)
    {
        return JSONRequest.fromString(json, JSONItemListRequest.class);
    }

    public boolean valid()
    {
        return this.getMessageName() ==
               JSONItemListRequest.message_name_valid &&
               this.user_id > 0 &&
               this.language != null &&
               this.language.length() >= 2 &&
               this.language.length() <= 3;
    }

    public long getUserId()
    {
        return this.user_id;
    }

    public String getLanguage()
    {
        return this.language;
    }

}

