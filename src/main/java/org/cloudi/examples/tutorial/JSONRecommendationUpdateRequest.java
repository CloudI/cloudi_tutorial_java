//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONRecommendationUpdateRequest extends JSONRequest
{
    public static final String message_name_valid = "recommendation_update";
    private long user_id;
    private long item_id;
    private double rating;

    public static JSONRecommendationUpdateRequest fromString(final String json)
    {
        return JSONRequest.fromString(json,
                                      JSONRecommendationUpdateRequest.class);
    }

    public boolean valid()
    {
        return this.getMessageName().equals(this.message_name_valid) &&
               this.user_id > 0 &&
               this.item_id > 0 &&
               this.rating >= LenskitData.RATING_MIN &&
               this.rating <= LenskitData.RATING_MAX;
    }

    public long getUserId()
    {
        return this.user_id;
    }

    public long getItemId()
    {
        return this.item_id;
    }

    public double getRating()
    {
        return this.rating;
    }

}

