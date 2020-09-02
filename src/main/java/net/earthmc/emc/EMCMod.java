package net.earthmc.emc;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.lwjgl.glfw.GLFW;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.Timer;
import java.util.TimerTask;

import net.earthmc.emc.utils.*;

public class EMCMod implements ModInitializer {
    JsonArray townless;
    int currentYOffset;

    ModConfig config;
    String[] colors;

    @Override
    public void onInitialize() // Called when Minecraft starts.
    {
        System.out.println("EarthMC Essentials Initialized!");

        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        
        KeyBinding f4 = KeyBindingHelper.registerKeyBinding(new KeyBinding("Townless Players", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F4, "EarthMC Essentials"));

        colors = new String[] {"BLUE","DARK_BLUE","GREEN","DARK_GREEN","AQUA","DARK_AQUA","RED","DARK_RED","LIGHT_PURPLE","DARK_PURPLE","YELLOW","GOLD","GRAY","DARK_GRAY","BLACK","WHITE"};

        //#region Create Townless Timer
        townless = getTownless();

        final Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() 
        {
            @Override
            public void run() 
            {
                townless = getTownless();
            }
        }, 0, 2*60*1000);
        //#endregion

        ClientTickEvents.END_CLIENT_TICK.register(client -> 
        {
            if (f4.isPressed())
            {
                ConfigBuilder builder = ConfigBuilder.create().setTitle("EarthMC Essentials Config").setTransparentBackground(true);

                ConfigCategory general = builder.getOrCreateCategory("General");
                ConfigCategory townless = builder.getOrCreateCategory("Townless");
    
                ConfigEntryBuilder entryBuilder = builder.entryBuilder();

                //#region Add Entries
                // Enable Mod
                general.addEntry(entryBuilder.startBooleanToggle("Enable Mod", config.general.enableMod)
                .setDefaultValue(true)
                .setTooltip("Toggles the mod on or off.")
                .setSaveConsumer(newValue -> config.general.enableMod = newValue)
                .build());

                // Enable EMC Only
                general.addEntry(entryBuilder.startBooleanToggle("EMC Only", config.general.emcOnly)
                .setDefaultValue(true)
                .setTooltip("Toggles EMC Only on or off. NOT YET IMPLEMENTED.")
                .setSaveConsumer(newValue -> config.general.emcOnly = newValue)
                .build());

                // Enable Townless
                general.addEntry(entryBuilder.startBooleanToggle("Enable Townless", config.general.enableTownless)
                .setDefaultValue(true)
                .setTooltip("Toggles Townless on or off.")
                .setSaveConsumer(newValue -> config.general.enableTownless = newValue)
                .build());

                // Enable Near To
                general.addEntry(entryBuilder.startBooleanToggle("Enable Near To", config.general.enableNearTo)
                .setDefaultValue(true)
                .setTooltip("Toggles Near To on or off. NOT YET IMPLEMENTED.")
                .setSaveConsumer(newValue -> config.general.enableNearTo = newValue)
                .build());

                // Townless List Y Offset
                townless.addEntry(entryBuilder.startIntSlider("Townless List Y Offset", config.townless.townlessListYOffset, 20, 450)
                .setDefaultValue(20)
                .setTooltip("The vertical offset (in pixels) of the townless list.")
                .setSaveConsumer(newValue -> config.townless.townlessListYOffset = newValue)
                .build());

                // Townless List X Offset
                townless.addEntry(entryBuilder.startIntSlider("Townless List X Offset", config.townless.townlessListXOffset, 5, 800)
                .setDefaultValue(5)
                .setTooltip("The horizontal offset (in pixels) of the townless list.")
                .setSaveConsumer(newValue -> config.townless.townlessListXOffset = newValue)
                .build());

                // Townless Text Y Offset
                townless.addEntry(entryBuilder.startIntSlider("Townless Text Y Offset", config.townless.townlessTextYOffset, 5, 450)
                .setDefaultValue(5)
                .setTooltip("The vertical offset (in pixels) of the 'Townless Players' text.")
                .setSaveConsumer(newValue -> config.townless.townlessTextYOffset = newValue)
                .build());

                // Townless Text X Offset
                townless.addEntry(entryBuilder.startIntSlider("Townless Text X Offset", config.townless.townlessTextXOffset, 5, 800)
                .setDefaultValue(5)
                .setTooltip("The horizontal offset (in pixels) of the 'Townless Players' text.")
                .setSaveConsumer(newValue -> config.townless.townlessTextXOffset = newValue)
                .build());

                // Townless Text Color
                townless.addEntry(entryBuilder.startSelector("Townless Text Color", colors, config.townless.townlessTextColor)
                .setDefaultValue("LIGHT_PURPLE")
                .setTooltip("The color of the 'Townless Players' text. Color names can be found at https://minecraft.gamepedia.com/Formatting_codes")
                .setSaveConsumer(newValue -> config.townless.townlessTextColor = newValue)
                .build());

                // Townless Player Color
                townless.addEntry(entryBuilder.startSelector("Townless Player Color", colors, config.townless.townlessPlayerColor)
                .setDefaultValue("LIGHT_PURPLE")
                .setTooltip("The color of the townless player names. Color names can be found at https://minecraft.gamepedia.com/Formatting_codes")
                .setSaveConsumer(newValue -> config.townless.townlessPlayerColor = newValue)
                .build());
                //#endregion
    
                Screen screen = builder.build();
                MinecraftClient.getInstance().openScreen(screen);
                
                builder.setSavingRunnable(() -> 
                {  
                    ConfigUtils.serializeConfig(config);
                });
			}
        });

        //#region HUDRendderCallback
        HudRenderCallback.EVENT.register(e -> 
        {     
            if (!config.general.enableMod || !config.general.enableTownless) return;

            // This is where the first player will be, who determines where the list will be.
            currentYOffset = config.townless.townlessListYOffset;

            // Create client & renderer
            final MinecraftClient client = MinecraftClient.getInstance();
            final TextRenderer renderer = client.textRenderer;
            
            Formatting townlessTextFormatting = Formatting.byName(config.townless.townlessTextColor);
            String townlessText = new LiteralText("Townless Players").formatted(townlessTextFormatting).asFormattedString();

            // Draw 'Townless Players' text.
            renderer.draw(townlessText, config.townless.townlessTextXOffset, config.townless.townlessTextYOffset, Formatting.WHITE.getColorValue());

            if (townless.size() >= 1)
            {            
                for (int i = 0; i < townless.size(); i++) 
                {
                    final JsonObject currentPlayer = (JsonObject) townless.get(i);

                    Formatting playerTextFormatting = Formatting.byName(config.townless.townlessPlayerColor);
                    String playerName = new LiteralText(currentPlayer.get("name").getAsString()).formatted(playerTextFormatting).asFormattedString();

                    final Integer playerX = currentPlayer.get("x").getAsInt();
                    final Integer playerY = currentPlayer.get("y").getAsInt();
                    final Integer playerZ = currentPlayer.get("z").getAsInt();

                    // If underground, display "Underground" instead of their position
                    if (playerX == 0 && playerZ == 0)
                    {
                        renderer.draw(playerName + " Underground", config.townless.townlessListXOffset, currentYOffset, Formatting.WHITE.getColorValue());
                    }
                    else 
                    {                   
                        renderer.draw(playerName + " " + playerX + ", " + playerY + ", " + playerZ, config.townless.townlessListXOffset, currentYOffset, Formatting.WHITE.getColorValue());
                    }

                    // Add offset for the next player.
                    currentYOffset += 10;
                }
            }
        });
        //#endregion
    }

    //#region Townless GET request
    static JsonArray getTownless()
    {
        try
        {
            final URL url = new URL("http://earthmc-api.herokuapp.com/townlessplayers");

            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            // Getting the response code
            final int responsecode = conn.getResponseCode();

            if (responsecode == 200) 
            {
                String inline = "";
                final Scanner scanner = new Scanner(url.openStream());

                // Write all the JSON data into a string using a scanner
                while (scanner.hasNext()) 
                {
                    inline += scanner.nextLine();
                }

                // Close the scanner
                scanner.close();

                // Using the JSON simple library parse the string into a json object
                final JsonParser parse = new JsonParser();
                final JsonArray townlessArray = (JsonArray) parse.parse(inline);
                    
                return townlessArray;
            }
        }
        catch (final Exception exc) 
        {
            return new JsonArray();
        }

        return new JsonArray();
    }
    //#endregion
}