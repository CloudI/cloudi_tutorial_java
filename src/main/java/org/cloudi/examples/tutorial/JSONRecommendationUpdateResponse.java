//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:

package org.cloudi.examples.tutorial;

import java.util.List;
import java.util.LinkedList;

public class JSONRecommendationUpdateResponse extends JSONResponse
{
    private final Long user_id;
    private final List<JSONRecommendation> recommendations;

    public static JSONRecommendationUpdateResponse
        success(final long user_id,
                final List<JSONRecommendation> recommendations)
    {
        return new JSONRecommendationUpdateResponse(true, null,
                                                    user_id, recommendations);
    }

    public static JSONRecommendationUpdateResponse
        failure(final String error,
                final long user_id)
    {
        final List<JSONRecommendation> empty =
            new LinkedList<JSONRecommendation>();
        return new JSONRecommendationUpdateResponse(false, error,
                                                    user_id, empty);
    }

    public static JSONRecommendationUpdateResponse
        failure(final String error)
    {
        final List<JSONRecommendation> empty =
            new LinkedList<JSONRecommendation>();
        return new JSONRecommendationUpdateResponse(false, error,
                                                    null, empty);
    }

    private JSONRecommendationUpdateResponse(final boolean success,
                                             final String error,
                                             final Long user_id,
                                             final List<JSONRecommendation>
                                                 recommendations)
    {
        super(JSONRecommendationUpdateRequest.message_name_valid,
              success, error);
        this.user_id = user_id;
        this.recommendations = recommendations;
    }

}

