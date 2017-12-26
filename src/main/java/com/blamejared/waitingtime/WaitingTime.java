package com.blamejared.waitingtime;

import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import org.apache.logging.log4j.LogManager;

@Mod(modid = "waitingtime", certificateFingerprint = "6919c86a9d8d117dbc520ba31378675eaae1c16a")
public class WaitingTime {
    
    @Mod.EventHandler
    public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
        LogManager.getLogger("waitingtime").warn("Invalid fingerprint detected! The file " + event.getSource().getName() + " may have been tampered with. This version will NOT be supported by the author!");
    }
}
