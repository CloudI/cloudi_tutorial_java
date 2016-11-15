//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:
package org.cloudi.examples.tutorial;

public class JSONRecommendationListRequest extends JSONRequest
{
    public static final String message_name_valid = "recommendation_list";
    private long user_id;

    public static JSONRecommendationListRequest fromString(final String json)
    {
        return JSONRequest.fromString(json,
                                      JSONRecommendationListRequest.class);
    }

    public boolean valid()
    {
        return this.getMessageName().equals(this.message_name_valid) &&
               this.user_id > 0;
    }

    public long getUserId()
    {
        return this.user_id;
    }

}

