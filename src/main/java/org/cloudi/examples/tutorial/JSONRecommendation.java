//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;

public class JSONRecommendation
{
    private final long item_id;
    private final double rating_expected;

    public JSONRecommendation(final long item_id,
                              final double rating_expected)
    {
        this.item_id = item_id;
        this.rating_expected = rating_expected;
    }

    public final long getItemId()
    {
        return this.item_id;
    }

    public final double getRatingExpected()
    {
        return this.rating_expected;
    }

    public static class Serializer implements JsonSerializer<JSONRecommendation>
    {
        @Override
        public JsonElement serialize(JSONRecommendation src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context)
        {
            JsonObject object = new JsonObject();
            object.addProperty("item_id", src.getItemId());
            object.addProperty("rating_expected", src.getRatingExpected());
            return object;
        }
    }

}

