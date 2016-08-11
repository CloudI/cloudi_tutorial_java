//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.util.List;
import java.util.LinkedList;

public class JSONRateResponse extends JSONResponse
{
    private final long user_id;
    private final List<JSONRateRecommendation> recommendations;

    public static JSONRateResponse success(final long user_id,
                                           final List<JSONRateRecommendation>
                                               recommendations)
    {
        return new JSONRateResponse(true, null, user_id, recommendations);
    }

    public static JSONRateResponse failure(final String error,
                                           final long user_id)
    {
        return new JSONRateResponse(false, error, user_id,
                                    new LinkedList<JSONRateRecommendation>());
    }

    private JSONRateResponse(final boolean success,
                             final String error,
                             final long user_id,
                             final List<JSONRateRecommendation> recommendations)
    {
        super(success, error);
        this.user_id = user_id;
        this.recommendations = recommendations;
    }

}

