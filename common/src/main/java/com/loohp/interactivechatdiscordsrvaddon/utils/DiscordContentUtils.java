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

package com.loohp.interactivechatdiscordsrvaddon.utils;

import com.loohp.interactivechat.InteractiveChat;
import com.loohp.interactivechat.libs.com.cryptomorin.xseries.XMaterial;
import com.loohp.interactivechat.libs.net.kyori.adventure.text.Component;
import com.loohp.interactivechat.objectholders.ICInventoryHolder;
import com.loohp.interactivechat.objectholders.ICPlayer;
import com.loohp.interactivechat.objectholders.OfflineICPlayer;
import com.loohp.interactivechat.utils.BookUtils;
import com.loohp.interactivechat.utils.FilledMapUtils;
import com.loohp.interactivechat.utils.InventoryUtils;
import com.loohp.interactivechat.utils.LanguageUtils;
import com.loohp.interactivechat.utils.MCVersion;
import com.loohp.interactivechatdiscordsrvaddon.InteractiveChatDiscordSrvAddon;
import com.loohp.interactivechatdiscordsrvaddon.debug.Debug;
import com.loohp.interactivechatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.interactivechatdiscordsrvaddon.graphics.ImageUtils;
import com.loohp.interactivechatdiscordsrvaddon.hooks.imageframe.ImageFrameHook;
import com.loohp.interactivechatdiscordsrvaddon.nms.NMSAddon;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.DiscordDisplayData;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.DiscordMessageContent;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.ImageDisplayData;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.ImageDisplayType;
import com.loohp.interactivechatdiscordsrvaddon.objectholders.ToolTipComponent;
import com.loohp.interactivechatdiscordsrvaddon.resources.CustomItemTextureRegistry;
import com.loohp.interactivechatdiscordsrvaddon.resources.ModelRenderer;
import com.loohp.interactivechatdiscordsrvaddon.resources.models.ModelDisplay.ModelDisplayPosition;
import com.loohp.interactivechatdiscordsrvaddon.utils.DiscordItemStackUtils.DiscordToolTip;
import com.loohp.interactivechatdiscordsrvaddon.wrappers.TitledInventoryWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.map.MapView;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class DiscordContentUtils {

    public static final Color OFFSET_WHITE = new Color(0xFFFFFE);

    public static List<DiscordMessageContent> createContents(List<DiscordDisplayData> dataList, OfflineICPlayer player) {
        List<DiscordMessageContent> contents = new ArrayList<>();
        int i = -1;
        for (DiscordDisplayData data : dataList) {
            i++;
            if (data instanceof ImageDisplayData) {
                ImageDisplayData iData = (ImageDisplayData) data;
                ImageDisplayType type = iData.getType();
                String title = iData.getTitle();
                if (iData.getItemStack().isPresent()) {
                    Debug.debug("createContents creating item discord content");
                    ItemStack item = iData.getItemStack().get();
                    Color color = DiscordItemStackUtils.getDiscordColor(item);
                    if (color == null || color.equals(Color.WHITE)) {
                        color = OFFSET_WHITE;
                    }
                    try {
                        BufferedImage image = ImageGeneration.getItemStackImage(item, data.getPlayer(), InteractiveChatDiscordSrvAddon.plugin.itemAltAir, 48);
                        byte[] imageData = ImageUtils.toArray(image);

                        DiscordMessageContent content = new DiscordMessageContent(title, null, color);
                        content.setTitle(DiscordItemStackUtils.getItemNameForDiscord(item, player, InteractiveChatDiscordSrvAddon.plugin.language));
                        content.setThumbnail("attachment://Item_" + i + ".png");

                        content.addAttachment("Item_" + i + ".png", imageData);
                        contents.add(content);

                        DiscordToolTip discordToolTip = DiscordItemStackUtils.getToolTip(item, player, InteractiveChatDiscordSrvAddon.plugin.showAdvanceDetails);
                        List<ToolTipComponent<?>> toolTipComponents = discordToolTip.isHideTooltip() ? new ArrayList<>() : discordToolTip.getComponents();

                        boolean forceShow = false;
                        if (type.equals(ImageDisplayType.ITEM_CONTAINER) && InteractiveChatDiscordSrvAddon.plugin.showContainers) {
                            TitledInventoryWrapper inv = iData.getInventory().get();
                            BufferedImage container = ImageGeneration.getInventoryImage(inv.getInventory(), inv.getTitle(), data.getPlayer());
                            toolTipComponents.add(ToolTipComponent.image(container));
                            forceShow = true;
                        } else if (iData.isFilledMap() && InteractiveChatDiscordSrvAddon.plugin.showMaps) {
                            forceShow = true;
                        }

                        if ((forceShow || !discordToolTip.isHideTooltip()) && (forceShow || !discordToolTip.isBaseItem() || InteractiveChatDiscordSrvAddon.plugin.itemUseTooltipImageOnBaseItem)) {
                            BufferedImage tooltip = ImageGeneration.getToolTipImage(toolTipComponents, NMSAddon.getInstance().getCustomTooltipResourceLocation(item));

                            if (InteractiveChatDiscordSrvAddon.plugin.showMaps) {
                                if (iData.isFilledMap()) {
                                    MapView mapView = FilledMapUtils.getMapView(item);
                                    boolean isContextual = mapView == null || FilledMapUtils.isContextual(mapView);
                                    ICPlayer icPlayer = iData.getPlayer().getPlayer();
                                    boolean isPlayerLocal = icPlayer != null && icPlayer.isLocal();
                                    if (!isContextual || isPlayerLocal) {
                                        BufferedImage map = ImageGeneration.getMapImage(item, isPlayerLocal ? icPlayer.getLocalPlayer() : null).get();
                                        tooltip = ImageUtils.resizeImage(tooltip, 5);
                                        tooltip = ImageUtils.appendImageBottom(tooltip, map, 10, 0);
                                    }
                                } else if (InteractiveChatDiscordSrvAddon.imageFrameHook && ImageFrameHook.isImageFrameCombinedImageItem(item)) {
                                    BufferedImage source = ImageFrameHook.getImageFrameCombinedImage(item);
                                    if (source != null) {
                                        BufferedImage map = ImageGeneration.getMapImage(source);
                                        tooltip = ImageUtils.resizeImage(tooltip, 5);
                                        tooltip = ImageUtils.appendImageBottom(tooltip, map, 10, 0);
                                    }
                                }
                            }

                            byte[] tooltipData = ImageUtils.toArray(tooltip);
                            content.addAttachment("ToolTip_" + i + ".png", tooltipData);
                            content.addImageUrl("attachment://ToolTip_" + i + ".png");
                        }

                        if (iData.isBook() && InteractiveChatDiscordSrvAddon.plugin.showBooks) {
                            BookMeta bookMeta = (BookMeta) item.getItemMeta();
                            List<Component> pages = BookUtils.getPages(bookMeta, player.isOnline() && player.getPlayer().isLocal() ? player.getPlayer().getLocalPlayer() : null);
                            if (pages.isEmpty()) {
                                pages = Collections.singletonList(Component.empty());
                            }
                            List<Supplier<BufferedImage>> pageImages = ImageGeneration.getBookInterfaceSuppliers(pages);
                            String bookTitle = bookMeta != null && bookMeta.hasTitle() ? bookMeta.getTitle() : DiscordItemStackUtils.getItemNameForDiscord(item, player, InteractiveChatDiscordSrvAddon.plugin.language);
                            for (int page = 0; page < pageImages.size(); page++) {
                                byte[] pageData = ImageUtils.toArray(pageImages.get(page).get());
                                DiscordMessageContent pageContent = new DiscordMessageContent(null, null, null, "attachment://Page_" + (page + 1) + ".png", color);
                                pageContent.setTitle(bookTitle + " (Page " + (page + 1) + ")");
                                pageContent.addAttachment("Page_" + (page + 1) + ".png", pageData);
                                contents.add(pageContent);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (iData.getInventory().isPresent()) {
                    Debug.debug("createContents creating inventory discord content");
                    TitledInventoryWrapper inv = iData.getInventory().get();
                    try {
                        BufferedImage image;
                        if (iData.isPlayerInventory()) {
                            if (InteractiveChatDiscordSrvAddon.plugin.usePlayerInvView) {
                                image = ImageGeneration.getPlayerInventoryImage(inv.getInventory(), iData.getPlayer());
                            } else {
                                image = ImageGeneration.getInventoryImage(inv.getInventory(), inv.getTitle(), data.getPlayer());
                            }
                        } else {
                            image = ImageGeneration.getInventoryImage(inv.getInventory(), inv.getTitle(), data.getPlayer());
                        }
                        Color color;
                        switch (type) {
                            case ENDERCHEST:
                                color = InteractiveChatDiscordSrvAddon.plugin.enderColor;
                                break;
                            case INVENTORY:
                                color = InteractiveChatDiscordSrvAddon.plugin.invColor;
                                break;
                            default:
                                color = Color.black;
                                break;
                        }
                        byte[] imageData = ImageUtils.toArray(image);
                        DiscordMessageContent content = new DiscordMessageContent(title, null, null, "attachment://Inventory_" + i + ".png", color);
                        content.addAttachment("Inventory_" + i + ".png", imageData);
                        if (type.equals(ImageDisplayType.INVENTORY) && InteractiveChatDiscordSrvAddon.plugin.invShowLevel) {
                            int level = iData.getPlayer().getExperienceLevel();
                            byte[] bottleData = ImageUtils.toArray(InteractiveChatDiscordSrvAddon.plugin.modelRenderer.render(32, 32, ModelRenderer.SINGLE_RENDER, InteractiveChatDiscordSrvAddon.plugin.getResourceManager(), InteractiveChatDiscordSrvAddon.plugin.getResourceManager().getResourceRegistry(CustomItemTextureRegistry.IDENTIFIER, CustomItemTextureRegistry.class).getItemPostResolveFunction("minecraft:item/experience_bottle", null, XMaterial.EXPERIENCE_BOTTLE.parseItem(), InteractiveChat.version.isOld(), null, null, null, null, InteractiveChatDiscordSrvAddon.plugin.getResourceManager().getLanguageManager().getTranslateFunction().ofLanguage(InteractiveChatDiscordSrvAddon.plugin.language)).orElse(null), InteractiveChat.version.isOld(), "minecraft:item/experience_bottle", ModelDisplayPosition.GUI, false, null, null).getImage(0));
                            content.addAttachment("Level_" + i + ".png", bottleData);
                            content.setFooter(ComponentStringUtils.convertFormattedString(LanguageUtils.getTranslation(TranslationKeyUtils.getLevelTranslation(level), InteractiveChatDiscordSrvAddon.plugin.language).getResult(), level));
                            content.setFooterImageUrl("attachment://Level_" + i + ".png");
                        }
                        contents.add(content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return contents;
    }

    public static Inventory getBlockInventory(ItemStack item) {
        Inventory inv = null;
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockState bsm = ((BlockStateMeta) item.getItemMeta()).getBlockState();
            if (bsm instanceof InventoryHolder) {
                Inventory container = ((InventoryHolder) bsm).getInventory();
                if (!isInventoryEmpty(container)) {
                    inv = Bukkit.createInventory(ICInventoryHolder.INSTANCE, InventoryUtils.toMultipleOf9(container.getSize()));
                    for (int j = 0; j < container.getSize(); j++) {
                        if (container.getItem(j) != null) {
                            if (!container.getItem(j).getType().equals(Material.AIR)) {
                                inv.setItem(j, container.getItem(j).clone());
                            }
                        }
                    }
                }
            }
        }
        return inv;
    }

    private static boolean isInventoryEmpty(Inventory inventory) {
        if (InteractiveChat.version.isNewerOrEqualTo(MCVersion.V1_16_2)) {
            return inventory.isEmpty();
        } else {
            return Arrays.stream(inventory.getContents()).noneMatch(i -> i != null && !i.getType().equals(Material.AIR));
        }
    }

}
