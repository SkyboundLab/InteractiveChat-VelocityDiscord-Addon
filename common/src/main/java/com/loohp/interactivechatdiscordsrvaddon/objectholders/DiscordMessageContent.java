/*
 * This file is part of InteractiveChatDiscordSrvAddon2.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
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

package com.loohp.interactivechatdiscordsrvaddon.objectholders;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiscordMessageContent {

    private String authorName;
    private String authorIconUrl;
    private String title;
    private List<String> description;
    private List<String> imageUrl;
    private String thumbnail;
    private List<Field> fields;
    private int color;
    private String footer;
    private String footerImageUrl;
    private Map<String, byte[]> attachments;

    public DiscordMessageContent(String authorName, String authorIconUrl, List<String> description, List<String> imageUrl, int color, Map<String, byte[]> attachments) {
        this.authorName = authorName;
        this.authorIconUrl = authorIconUrl;
        this.description = description;
        this.imageUrl = imageUrl;
        this.fields = new ArrayList<>();
        this.color = color;
        this.attachments = attachments;
        this.footer = null;
        this.footerImageUrl = null;
    }

    public DiscordMessageContent(String authorName, String authorIconUrl, String description, String imageUrl, Color color) {
        this(authorName, authorIconUrl, singletonOrEmpty(description), singletonOrEmpty(imageUrl), color.getRGB(), new HashMap<>());
    }

    public DiscordMessageContent(String authorName, String authorIconUrl, String description, String imageUrl, int color) {
        this(authorName, authorIconUrl, singletonOrEmpty(description), singletonOrEmpty(imageUrl), color, new HashMap<>());
    }

    /** A {@code null} value must produce an empty list, never a list holding a null element (JDA omits null descriptions; JSON does not). */
    private static List<String> singletonOrEmpty(String value) {
        return value == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(value));
    }

    public DiscordMessageContent(String authorName, String authorIconUrl, Color color) {
        this(authorName, authorIconUrl, new ArrayList<>(), new ArrayList<>(), color.getRGB(), new HashMap<>());
    }

    public DiscordMessageContent(String authorName, String authorIconUrl, int color) {
        this(authorName, authorIconUrl, new ArrayList<>(), new ArrayList<>(), color, new HashMap<>());
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorIconUrl() {
        return authorIconUrl;
    }

    public void setAuthorIconUrl(String authorIconUrl) {
        this.authorIconUrl = authorIconUrl;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getDescriptions() {
        return description;
    }

    public void setDescriptions(List<String> description) {
        this.description = description;
    }

    public void addDescription(String description) {
        this.description.add(description);
    }

    public void setDescription(int index, String description) {
        this.description.set(index, description);
    }

    public void clearDescriptions() {
        description.clear();
    }

    public List<String> getImageUrls() {
        return imageUrl;
    }

    public void setImageUrls(List<String> imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void addImageUrl(String imageUrl) {
        this.imageUrl.add(imageUrl);
    }

    public void setImageUrl(int index, String imageUrl) {
        this.imageUrl.set(index, imageUrl);
    }

    public void clearImageUrls() {
        imageUrl.clear();
    }

    public List<Field> getFields() {
        return fields;
    }

    public void addFields(Field... field) {
        fields.addAll(Arrays.asList(field));
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getFooterImageUrl() {
        return footerImageUrl;
    }

    public void setFooterImageUrl(String footerImageUrl) {
        this.footerImageUrl = footerImageUrl;
    }

    public Map<String, byte[]> getAttachments() {
        return attachments;
    }

    public void setAttachments(Map<String, byte[]> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(String name, byte[] data) {
        attachments.put(name, data);
    }

    public void clearAttachments() {
        attachments.clear();
    }

    public static class Field {

        private final String name;
        private final String value;
        private final boolean inline;

        public Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isInline() {
            return inline;
        }

    }

}
