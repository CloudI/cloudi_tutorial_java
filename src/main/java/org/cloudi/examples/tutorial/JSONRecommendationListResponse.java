//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.util.List;
import java.util.LinkedList;

public class JSONRecommendationListResponse extends JSONResponse
{
    private final long user_id;
    private final List<JSONRecommendation> recommendations;

    public static JSONRecommendationListResponse
        success(final long user_id,
                final List<JSONRecommendation> recommendations)
    {
        return new JSONRecommendationListResponse(true, null,
                                                  user_id, recommendations);
    }

    public static JSONRecommendationListResponse
        failure(final String error,
                final long user_id)
    {
        final List<JSONRecommendation> empty =
            new LinkedList<JSONRecommendation>();
        return new JSONRecommendationListResponse(false, error,
                                                  user_id, empty);
    }

    private JSONRecommendationListResponse(final boolean success,
                                           final String error,
                                           final long user_id,
                                           final List<JSONRecommendation>
                                               recommendations)
    {
        super(success, error);
        this.user_id = user_id;
        this.recommendations = recommendations;
    }

}

