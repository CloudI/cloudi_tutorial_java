//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONRecommendationRefreshResponse extends JSONResponse
{
    public static JSONRecommendationRefreshResponse fromString(final String
                                                                   json)
    {
        return JSON.object()
                   .fromJson(json, JSONRecommendationRefreshResponse.class);
    }

    public static JSONRecommendationRefreshResponse success()
    {
        return new JSONRecommendationRefreshResponse(true, null);
    }

    public static JSONRecommendationRefreshResponse failure(final String error)
    {
        return new JSONRecommendationRefreshResponse(false, error);
    }

    private JSONRecommendationRefreshResponse(final boolean success,
                                              final String error)
    {
        super(JSONRecommendationRefreshRequest.message_name_valid,
              success, error);
    }

}

