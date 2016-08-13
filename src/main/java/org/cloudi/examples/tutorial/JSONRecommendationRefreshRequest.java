//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONRecommendationRefreshRequest extends JSONRequest
{
    public static final String message_name_valid = "recommendation_refresh";

    public static JSONRecommendationRefreshRequest fromString(final String json)
    {
        return JSONRequest.fromString(json,
                                      JSONRecommendationRefreshRequest.class);
    }

    public boolean valid()
    {
        return this.getMessageName() ==
               JSONRecommendationRefreshRequest.message_name_valid;
    }

}

