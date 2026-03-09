package com.chedd.dragontweaks;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(DragonTweaks.MODID)
public class DragonTweaks {
    public static final String MODID = "dragontweaks";
    private static final Logger LOGGER = LogUtils.getLogger();

    public DragonTweaks(FMLJavaModLoadingContext context) {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Dragon Tweaks loaded - seeker staff now finds only mother dragons!");
    }
}
