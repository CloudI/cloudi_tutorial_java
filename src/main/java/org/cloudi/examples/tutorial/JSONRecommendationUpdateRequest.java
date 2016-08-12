//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

public class JSONRecommendationUpdateRequest
{
    private long user_id;
    private long item_id;
    private double rating;

    public static JSONRecommendationUpdateRequest fromString(final String json)
    {
        return JSON.object()
                   .fromJson(json, JSONRecommendationUpdateRequest.class);
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

