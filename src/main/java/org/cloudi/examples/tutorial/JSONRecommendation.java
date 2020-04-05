//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:

package org.cloudi.examples.tutorial;

import java.util.Arrays;
import java.lang.reflect.Type;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;

public class JSONRecommendation
{
    private final long item_id;
    private final String creator;
    private final String creator_link;
    private final String title;
    private final String date_created;
    private final String[] languages;
    private final String[] subjects;
    private final Integer downloads;
    private final Double rating;
    private final double rating_expected;

    public JSONRecommendation(final long item_id,
                              final String creator,
                              final String creator_link,
                              final String title,
                              final String date_created,
                              final String[] languages,
                              final String[] subjects,
                              final Integer downloads,
                              final Double rating,
                              final double rating_expected)
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
        this.rating_expected = rating_expected;
    }

    public long getItemId()
    {
        return this.item_id;
    }

    public String getCreator()
    {
        return this.creator;
    }

    public String getCreatorLink()
    {
        return this.creator_link;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getDateCreated()
    {
        return this.date_created;
    }

    public String[] getLanguages()
    {
        return this.languages;
    }

    public String[] getSubjects()
    {
        return this.subjects;
    }

    public Integer getDownloads()
    {
        return this.downloads;
    }

    public Double getRating()
    {
        return this.rating;
    }

    public double getRatingExpected()
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
            final JsonObject object = new JsonObject();
            object.addProperty("item_id", src.getItemId());
            object.addProperty("creator", src.getCreator());
            object.addProperty("creator_link", src.getCreatorLink());
            object.addProperty("title", src.getTitle());
            object.addProperty("date_created", src.getDateCreated());
            object.add("languages",
                JSON.object().toJsonTree(src.getLanguages()));
            object.add("subjects",
                JSON.object().toJsonTree(src.getSubjects()));
            object.addProperty("downloads", src.getDownloads());
            object.addProperty("rating", src.getRating());
            object.addProperty("rating_expected", src.getRatingExpected());
            return object;
        }
    }

}

