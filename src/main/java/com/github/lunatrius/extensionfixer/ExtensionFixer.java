package com.github.lunatrius.extensionfixer;

import com.github.lunatrius.extensionfixer.reference.Reference;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod(modid = Reference.MODID, name = Reference.NAME, version = Reference.VERSION)
public class ExtensionFixer {
    public ExtensionFixer() {
        final List<File> pendingRename = new ArrayList<File>();
        final ModClassLoader classLoader = (ModClassLoader) Loader.instance().getModClassLoader();
        final File[] sources = classLoader.getParentSources();

        for (final File file : sources) {
            final String name = file.getName();
            if (name.toLowerCase().endsWith(".jar.zip")) {
                Reference.logger.info("Scheduling rename for: " + name);
                pendingRename.add(file);
            }
        }

        if (pendingRename.size() > 0) {
            Runtime.getRuntime().addShutdownHook(new Thread("Rename Mods") {
                @Override
                public void run() {
                    for (final File file : pendingRename) {
                        final boolean success = file.renameTo(new File(file.getParent(), file.getName().replaceFirst("(?i).jar.zip", ".jar")));
                        Reference.logger.info("Renamed " + file.getName() + ": " + (success ? "yes" : "no"));
                    }
                }
            });

            StringBuilder message = new StringBuilder();
            message.append("Minecraft will now stop running and ").append(Reference.NAME);
            message.append("\nwill try to fix the file extensions for the following mods:\n");

            for (final File file : pendingRename) {
                message.append("  - ").append(file.getName()).append("\n");
            }

            message.append("\nPLEASE RESTART MINECRAFT AFTER CLICKING 'OK'\n\n");
            message.append("If this message keeps appearing you'll have to remove\n");
            message.append(Reference.NAME).append(" and manually rename the mods.");

            Reference.logger.error(Reference.MODID + " has found files with incorrect file extensions, shutting down and trying to rename them...");
            showMessage(message.toString(), Reference.NAME);
            FMLCommonHandler.instance().exitJava(1, false);
        }
    }

    @NetworkCheckHandler
    public boolean checkModList(Map<String, String> versions, Side side) {
        return true;
    }

    private static void showMessage(final String message, final String title) {
        JOptionPane pane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(title);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }
}
