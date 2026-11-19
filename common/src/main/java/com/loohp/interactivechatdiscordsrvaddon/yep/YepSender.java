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

import com.loohp.interactivechatdiscordsrvaddon.InteractiveChatDiscordSrvAddon;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.DiscordMessageContent;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class YepSender {

    private static final int CHUNK_SIZE = 20000;
    private static final int MAX_EMBEDS_PER_MESSAGE = 10;
    private static final int MAX_FILES_PER_MESSAGE = 10;

    private YepSender() {}

    public static void send(Player receiver, String username, UUID uuid, String text, List<DiscordMessageContent> contents) {
        List<DiscordMessageContent> list = contents == null ? Collections.emptyList() : contents;

        String message = text == null ? "" : text;
        if (message.trim().isEmpty() && list.isEmpty()) return;

        boolean first = true;
        for (List<DiscordMessageContent> batch : batch(list)) {
            List<String> embeds = new ArrayList<>();
            Map<String, byte[]> attachments = new LinkedHashMap<>();
            for (DiscordMessageContent content : batch) {
                embeds.addAll(EmbedJson.toJsonList(content));
                addAttachments(attachments, content);
            }

            String transferId = UUID.randomUUID().toString().replace("-", "");
            sendAttachments(receiver, transferId, attachments);

            byte[] payload = YepProtocol.discordPayload(username, uuid == null ? "" : uuid.toString(), first ? message : "", toJsonArray(embeds));
            if (payload == null) logWarning("Message from " + username + " is too large to relay, skipping");
            else receiver.sendPluginMessage(plugin(), YepProtocol.YEP_GENERIC_CHANNEL, payload);

            first = false;
        }
    }

    private static List<List<DiscordMessageContent>> batch(List<DiscordMessageContent> contents) {
        List<List<DiscordMessageContent>> batches = new ArrayList<>();
        List<DiscordMessageContent> current = new ArrayList<>();

        int embeds = 0;

        for (DiscordMessageContent content : contents) {
            int size = EmbedJson.toJsonList(content).size();
            if (!current.isEmpty() && embeds + size > MAX_EMBEDS_PER_MESSAGE) {
                batches.add(current);
                current = new ArrayList<>();
                embeds = 0;
            }

            current.add(content);
            embeds += size;
        }

        if (current.isEmpty()) batches.add(Collections.emptyList());
        else batches.add(current);

        return batches;
    }

    private static String toJsonArray(List<String> embeds) {
        if (embeds.isEmpty()) return "";

        List<String> limited = embeds.size() > MAX_EMBEDS_PER_MESSAGE ? embeds.subList(0, MAX_EMBEDS_PER_MESSAGE) : embeds;

        return limited.size() == 1 ? limited.get(0) : "[" + String.join(",", limited) + "]";
    }

    private static void addAttachments(Map<String, byte[]> attachments, DiscordMessageContent content) {
        if (content.getAttachments() == null) return;

        for (Map.Entry<String, byte[]> entry : content.getAttachments().entrySet()) {
            if (attachments.size() >= MAX_FILES_PER_MESSAGE) return;

            byte[] data = entry.getValue();

            if (data != null && data.length > 0) attachments.put(entry.getKey(), data);
        }
    }

    private static void sendAttachments(Player receiver, String transferId, Map<String, byte[]> attachments) {
        Base64.Encoder encoder = Base64.getEncoder();

        for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
            String encoded = encoder.encodeToString(entry.getValue());

            int total = (encoded.length() + CHUNK_SIZE - 1) / CHUNK_SIZE;
            for (int index = 0; index < total; index++) {
                String chunk = encoded.substring(index * CHUNK_SIZE, Math.min((index + 1) * CHUNK_SIZE, encoded.length()));

                byte[] payload = YepProtocol.attachmentPayload(transferId, entry.getKey(), index, total, chunk);
                if (payload.length > YepProtocol.MAX_PAYLOAD_BYTES) logWarning("Attachment chunk for " + entry.getKey() + " is too large to relay, skipping");
                else receiver.sendPluginMessage(plugin(), YepProtocol.YEP_GENERIC_CHANNEL, payload);
            }
        }
    }

    private static InteractiveChatDiscordSrvAddon plugin() {
        return InteractiveChatDiscordSrvAddon.plugin;
    }

    private static void logWarning(String message) {
        InteractiveChatDiscordSrvAddon instance = plugin();
        if (instance != null) instance.getLogger().log(Level.WARNING, message);
    }

}
