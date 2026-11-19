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

package com.loohp.interactivechatdiscordsrvaddon.yep;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.api.events.PreChatPacketSendEvent;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import com.loohp.interactivechat.listeners.packet.MessagePacketHandler.PacketAccessorResult;
import com.loohp.interactivechat.objectholders.ICInventoryHolder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.ICPlayerFactory;
import com.loohp.interactivechat.platform.packets.PlatformPlayServerUnifiedChatMessagePacket;
import com.loohp.interactivechat.utils.ComponentFlattening;
import com.loohp.interactivechat.utils.InteractiveChatComponentSerializer;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.PlaceholderParser;
import com.loohp.interactivechat.utils.PlayerUtils;
import com.loohp.interactivechatdiscordsrvaddon.InteractiveChatDiscordSrvAddon;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.DiscordDisplayData;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.DiscordMessageContent;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.ImageDisplayData;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.ImageDisplayType;
import com.loohp.interactivechatdiscordsrvaddon.utils.ComponentStringUtils;
import com.loohp.interactivechatdiscordsrvaddon.utils.DiscordContentUtils;
import com.loohp.interactivechatdiscordsrvaddon.utils.TranslationKeyUtils;
import com.loohp.interactivechatdiscordsrvaddon.wrappers.TitledInventoryWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YepGameListener implements Listener {

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("[§&][0-9a-fk-orx]", Pattern.CASE_INSENSITIVE);
    private static final long DEDUPE_WINDOW_MILLIS = 5000L;

    private final Map<String, Long> recentMessages = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPreChatPacketSend(PreChatPacketSendEvent event) {
        try {
            UUID senderId = event.getSender();
            if (senderId == null) return;

            Player sender = Bukkit.getPlayer(senderId);
            if (sender == null) return;

            Component processedComponent = event.getComponent();
            if (processedComponent == null) return;

            String processedPlain = plainText(processedComponent);

            Component originalComponent = null;
            PlatformPlayServerUnifiedChatMessagePacket<?> originalPacket = event.getOriginal();
            if (originalPacket != null) {
                PacketAccessorResult result = originalPacket.read(event.getReceiver());
                if (result != null) originalComponent = result.getComponent();
            }

            String originalPlain = originalComponent == null ? processedPlain : plainText(originalComponent);
            String sourceMessage = originalPlain;

            ICPlayer icSender = ICPlayerFactory.getICPlayer(sender);
            if (icSender == null) return;

            List<DiscordDisplayData> dataList = new ArrayList<>();

            if (InteractiveChatDiscordSrvAddon.plugin.itemImage && InteractiveChat.useItem && InteractiveChat.itemPlaceholder != null) {
                Matcher matcher = InteractiveChat.itemPlaceholder.getKeyword().matcher(sourceMessage);
                if (matcher.find()) {
                    ItemStack item = PlayerUtils.getHeldItem(icSender);
                    if (item != null && item.getType() != Material.AIR) dataList.add(new ImageDisplayData(icSender, matcher.start(), title(icSender, InteractiveChat.itemTitle), ImageDisplayType.ITEM, item.clone()));
                }
            }

            if (InteractiveChatDiscordSrvAddon.plugin.invImage && InteractiveChat.useInventory && InteractiveChat.invPlaceholder != null) {
                Matcher matcher = InteractiveChat.invPlaceholder.getKeyword().matcher(sourceMessage);
                if (matcher.find()) {
                    int size = InventoryUtils.toMultipleOf9(icSender.getInventory().getSize());
                    if (size > 36) size = 45;

                    Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, size);
                    for (int j = 0; j < icSender.getInventory().getSize(); j++) {
                        ItemStack each = icSender.getInventory().getItem(j);
                        if (each != null && each.getType() != Material.AIR) inv.setItem(j, each.clone());
                    }

                    dataList.add(new ImageDisplayData(icSender, matcher.start(), title(icSender, InteractiveChat.invTitle), ImageDisplayType.INVENTORY, true, new TitledInventoryWrapper(Component.translatable(TranslationKeyUtils.getDefaultContainerTitle()), inv)));
                }
            }

            if (InteractiveChatDiscordSrvAddon.plugin.enderImage && InteractiveChat.useEnder && InteractiveChat.enderPlaceholder != null) {
                Matcher matcher = InteractiveChat.enderPlaceholder.getKeyword().matcher(sourceMessage);
                if (matcher.find()) {
                    Inventory inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.toMultipleOf9(icSender.getEnderChest().getSize()));
                    for (int j = 0; j < icSender.getEnderChest().getSize(); j++) {
                        ItemStack each = icSender.getEnderChest().getItem(j);
                        if (each != null && each.getType() != Material.AIR) inv.setItem(j, each.clone());
                    }

                    dataList.add(new ImageDisplayData(icSender, matcher.start(), title(icSender, InteractiveChat.enderTitle), ImageDisplayType.ENDERCHEST, new TitledInventoryWrapper(Component.translatable(TranslationKeyUtils.getEnderChestContainerTitle()), inv)));
                }
            }

            String replacementText = null;
            if (dataList.isEmpty()) {
                replacementText = replacementOnly(originalPlain, processedPlain);
                if (replacementText.isEmpty()) return;
            }

            long now = System.currentTimeMillis();
            recentMessages.entrySet().removeIf(entry -> now - entry.getValue() > DEDUPE_WINDOW_MILLIS);
            String dedupeKey = senderId + ":" + sourceMessage;
            if (recentMessages.putIfAbsent(dedupeKey, now) != null) return;

            if (dataList.isEmpty()) YepSender.send(event.getReceiver(), sender.getName(), senderId, replacementText, Collections.emptyList());
            else {
                dataList.sort(Comparator.comparingInt(DiscordDisplayData::getPosition));
                List<DiscordMessageContent> contents = DiscordContentUtils.createContents(dataList, icSender);
                YepSender.send(event.getReceiver(), sender.getName(), senderId, "", contents);
            }
        } catch (Throwable t) {
            InteractiveChatDiscordSrvAddon.plugin.getLogger().log(Level.WARNING, "Failed to relay InteractiveChat chat message to Discord", t);
        }
    }

    private static String plainText(Component component) {
        if (component == null) return "";

        return COLOR_CODE_PATTERN.matcher(InteractiveChatComponentSerializer.plainText().serialize(ComponentFlattening.flatten(component))).replaceAll("");
    }

    private static String title(ICPlayer player, String template) {
        return ComponentStringUtils.stripColorAndConvertMagic(PlaceholderParser.parse(player, template));
    }

    private static String replacementOnly(String original, String processed) {
        if (original == null || original.isEmpty() || original.equals(processed)) return processed;

        int max = Math.min(original.length(), processed.length());
        int start = 0;
        while (start < max && original.charAt(start) == processed.charAt(start)) start++;

        int originalEnd = original.length();
        int processedEnd = processed.length();
        while (originalEnd > start && processedEnd > start && original.charAt(originalEnd - 1) == processed.charAt(processedEnd - 1)) {
            originalEnd--;
            processedEnd--;
        }

        return processed.substring(start, processedEnd).trim();
    }

}