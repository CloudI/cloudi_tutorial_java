//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:

package org.cloudi.examples.tutorial;

public class JSONRecommendationRefreshOccurredNotification extends JSONResponse
{
    public static final String message_name_valid =
        "recommendation_refresh_occurred";

    public static JSONRecommendationRefreshOccurredNotification success()
    {
        return new JSONRecommendationRefreshOccurredNotification(true, null);
    }

    private JSONRecommendationRefreshOccurredNotification(final boolean success,
                                                          final String error)
    {
        super(JSONRecommendationRefreshOccurredNotification.message_name_valid,
              success, error);
    }

}

