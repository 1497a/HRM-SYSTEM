package com.hrm.util;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class IconUtil {
    private static final Path RESOURCE_ICON_DIR = Path.of("src", "main", "resources", "icons");
    private static final Map<String, BufferedImage> BASE_CACHE = new ConcurrentHashMap<>();

    private IconUtil() {
    }

    public static ImageIcon loadMenuIcon(String name, Color color) {
        BufferedImage base = loadBaseIcon(name);
        if (base == null) {
            return null;
        }
        BufferedImage scaled = resize(base, 18, 18);
        return new ImageIcon(tint(scaled, color));
    }

    private static BufferedImage loadBaseIcon(String name) {
        return BASE_CACHE.computeIfAbsent(name, key -> {
            String resourcePath = "/icons/" + key + ".png";
            try (InputStream in = IconUtil.class.getResourceAsStream(resourcePath)) {
                if (in != null) {
                    return ImageIO.read(in);
                }
            } catch (IOException ignored) {
            }
            Path filePath = RESOURCE_ICON_DIR.resolve(key + ".png");
            if (!Files.exists(filePath)) {
                return null;
            }
            try (InputStream in = Files.newInputStream(filePath)) {
                return ImageIO.read(in);
            } catch (IOException ignored) {
                return null;
            }
        });
    }

    private static BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(source, 0, 0, width, height, null);
        g2.dispose();
        return result;
    }

    private static BufferedImage tint(BufferedImage source, Color color) {
        BufferedImage tinted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = tinted.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(source, 0, 0, null);
        g2.setComposite(java.awt.AlphaComposite.SrcAtop);
        g2.setColor(color);
        g2.fillRect(0, 0, source.getWidth(), source.getHeight());
        g2.dispose();
        return tinted;
    }
}
