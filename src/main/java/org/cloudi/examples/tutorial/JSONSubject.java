//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:
package org.cloudi.examples.tutorial;

import java.lang.reflect.Type;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSerializationContext;

public class JSONSubject
{
    private final String subject;

    public JSONSubject(final String subject)
    {
        this.subject = subject;
    }

    public String getSubject()
    {
        return this.subject;
    }

    public static class Serializer implements JsonSerializer<JSONSubject>
    {
        @Override
        public JsonElement serialize(JSONSubject src,
                                     Type typeOfSrc,
                                     JsonSerializationContext context)
        {
            final JsonObject object = new JsonObject();
            object.addProperty("subject", src.getSubject());
            return object;
        }
    }

}

