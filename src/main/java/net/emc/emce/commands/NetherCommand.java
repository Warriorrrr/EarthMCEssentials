package net.emc.emce.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.emc.emce.utils.MsgUtils;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.minecraft.util.Formatting;

import static net.emc.emce.EMCE.client;

public class NetherCommand 
{
    public static void register() {
        ClientCommandManager.DISPATCHER.register(
            ClientCommandManager.literal("nether").then(
                ClientCommandManager.argument("x", IntegerArgumentType.integer()).then(
                    ClientCommandManager.argument("z", IntegerArgumentType.integer()).executes(c -> {

                    int x = IntegerArgumentType.getInteger(c, "x");
                    int z = IntegerArgumentType.getInteger(c, "z");

                    MsgUtils.sendPlayer("msg_nether_success", false, Formatting.GOLD, true, x/8, z/8);

                    return 1;
                })
            ).executes(c -> {
                MsgUtils.sendPlayer("msg_nether_err_args", false, Formatting.RED, true);
                return 1;
            })).executes(c -> {
                int x, z;

                if (client.player != null) {
                    x = (int) client.player.getX();
                    z = (int) client.player.getZ();

                    MsgUtils.sendPlayer("msg_nether_owncoords", false, Formatting.GRAY, true);
                    MsgUtils.sendPlayer("msg_nether_success", false, Formatting.GOLD, true, x/8, z/8);
                }
                else
                    MsgUtils.sendPlayer("msg_nether_err_null", false, Formatting.RED, true);

                return 1;
            })
        );
    }
}