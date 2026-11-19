/*
 * This file is part of InteractiveChatVelocityDiscordAddon.
 *
 * Copyright (C) 2020 - 2026. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2026. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechatdiscordsrvaddon.yep;

import com.loohp.interactivechat.libs.org.json.simple.JSONArray;
import com.loohp.interactivechat.libs.org.json.simple.JSONObject;
import com.loohp.interactivechat.libs.org.json.simple.JSONValue;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.DiscordMessageContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class EmbedJson {

    private EmbedJson() {}

    public static List<String> toJsonList(DiscordMessageContent content) {
        List<String> images = content.getImageUrls() == null ? Collections.emptyList() : content.getImageUrls().stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (images.isEmpty()) return Collections.singletonList(toJson(build(content, null)));

        List<String> result = new ArrayList<>(images.size());

        for (String image : images)
            result.add(toJson(build(content, image)));

        return result;
    }

    private static JSONObject build(DiscordMessageContent content, String image) {
        JSONObject embed = new JSONObject();

        put(embed, "title", content.getTitle());
        put(embed, "description", descriptions(content));

        embed.put("color", content.getColor());

        putObject(embed, "thumbnail", "url", content.getThumbnail());
        putObject(embed, "image", "url", image);

        if (!isBlank(content.getAuthorName())) {
            JSONObject author = new JSONObject();

            author.put("name", clean(content.getAuthorName()));
            put(author, "icon_url", content.getAuthorIconUrl());
            embed.put("author", author);
        }

        if (!isBlank(content.getFooter())) {
            JSONObject footer = new JSONObject();

            footer.put("text", clean(content.getFooter()));
            put(footer, "icon_url", content.getFooterImageUrl());
            embed.put("footer", footer);
        }

        if (content.getFields() != null && !content.getFields().isEmpty()) {
            JSONArray fields = new JSONArray();
            for (DiscordMessageContent.Field field : content.getFields()) {
                if (isBlank(field.getName()) || isBlank(field.getValue())) continue;

                JSONObject object = new JSONObject();

                object.put("name", clean(field.getName()));
                object.put("value", clean(field.getValue()));
                object.put("inline", field.isInline());
                fields.add(object);
            }

            if (!fields.isEmpty()) embed.put("fields", fields);
        }

        return embed;
    }

    private static String descriptions(DiscordMessageContent content) {
        if (content.getDescriptions() == null) return "";

        return content.getDescriptions().stream().filter(Objects::nonNull).collect(Collectors.joining("\n"));
    }

    private static void put(JSONObject object, String key, String value) {
        if (!isBlank(value)) object.put(key, clean(value));
    }

    private static void putObject(JSONObject object, String key, String innerKey, String value) {
        if (!isBlank(value)) {
            JSONObject inner = new JSONObject();

            inner.put(innerKey, clean(value));

            object.put(key, inner);
        }
    }

    private static String toJson(JSONObject object) {
        return JSONValue.toJSONString(object);
    }

    private static String clean(String value) {
        return YepProtocol.sanitize(value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}