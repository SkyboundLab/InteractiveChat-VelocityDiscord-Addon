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

import java.nio.charset.StandardCharsets;

public final class YepProtocol {

    public static final String YEP_GENERIC_CHANNEL = "yep:generic";
    public static final char RECORD_SEPARATOR = '␞';
    public static final char UNIT_SEPARATOR = '␟';
    public static final int MAX_PAYLOAD_BYTES = 32767;

    private static final String DISCORD_PREFIX = "interactivechat:discord" + RECORD_SEPARATOR;
    private static final String ATTACHMENT_PREFIX = "interactivechat:attachment" + RECORD_SEPARATOR;

    private YepProtocol() {}

    public static byte[] discordPayload(String username, String uuid, String text, String embedJson) {
        String head = DISCORD_PREFIX + sanitize(username) + UNIT_SEPARATOR + sanitize(uuid) + UNIT_SEPARATOR;
        String tail = UNIT_SEPARATOR + UNIT_SEPARATOR + (embedJson == null ? "" : embedJson);

        int textBytes = MAX_PAYLOAD_BYTES - utf8Length(head) - utf8Length(tail);
        if (textBytes <= 0) return null;

        return (head + truncate(sanitize(text), textBytes) + tail).getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] attachmentPayload(String transferId, String fileName, int index, int total, String base64Chunk) {
        String message = ATTACHMENT_PREFIX + sanitize(transferId) + UNIT_SEPARATOR + sanitize(fileName) + UNIT_SEPARATOR + index + UNIT_SEPARATOR + total + UNIT_SEPARATOR + base64Chunk;
        return message.getBytes(StandardCharsets.UTF_8);
    }

    public static String sanitize(String input) {
        if (input == null) return "";

        return input.replace(RECORD_SEPARATOR, ' ').replace(UNIT_SEPARATOR, ' ');
    }

    private static String truncate(String text, int maxBytes) {
        if (utf8Length(text) <= maxBytes) return text;

        int budget = maxBytes - utf8Length("…");
        if (budget <= 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int bytes = 0;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            String character = new String(Character.toChars(codePoint));

            int length = utf8Length(character);
            if (bytes + length > budget) break;

            result.append(character);
            bytes += length;
            i += Character.charCount(codePoint);
        }

        return result.append('…').toString();
    }

    private static int utf8Length(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

}