//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;

public class JSONItem
{
    private final long item_id;
    private final String creator;
    private final String creator_link;
    private final String title;
    private final String date_created;
    private final String[] languages;
    private final String[] subjects;
    private final int downloads;
    private final double rating;

    public JSONItem(final long item_id,
                    final String creator,
                    final String creator_link,
                    final String title,
                    final String date_created,
                    final String[] languages,
                    final String[] subjects,
                    final int downloads,
                    final double rating)
    {
        this.item_id = item_id;
        this.creator = creator;
        this.creator_link = creator_link;
        this.title = title;
        this.date_created = date_created;
        this.languages = languages;
        this.subjects = subjects;
        this.downloads = downloads;
        this.rating = rating;
    }

    public final long getItemId()
    {
        return this.item_id;
    }

    public final double getRating()
    {
        return this.rating;
    }

    public static class Serializer implements JsonSerializer<JSONItem>
    {
        @Override
        public JsonElement serialize(JSONItem src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context)
        {
            JsonObject object = new JsonObject();
            object.addProperty("item_id", src.getItemId());
            object.addProperty("rating", src.getRating());
            return object;
        }
    }

}

