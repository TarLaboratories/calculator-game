package com.calcgame.main.rendering;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

public class FontTexture {
    public record CharInfo(int startX, int width) {

    }

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String IMAGE_FORMAT = "PNG";
    private final Font font;
    private final String charSetName;
    private final Map<Character, CharInfo> charMap;
    private Texture texture;

    public FontTexture(Font font, String charSetName) {
        this.font = font;
        this.charSetName = charSetName;
        charMap = new HashMap<>();

        try {
            buildTexture();
        } catch (IOException e) {
            LOGGER.error("Failed to load font texture for charset {} and font {}!", charSetName, font.getFontName());
        }
    }

    public static FontTexture loadFont(String fontFileName) {
        try (InputStream stream = FontTexture.class.getResourceAsStream(Resources.font(fontFileName))) {
            if (stream == null) {
                LOGGER.warn("Font file {} not found! Using default font.", fontFileName);
                return loadFont("font");
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            font = font.deriveFont(16f);
            return new FontTexture(font, "ISO-8859-1");
        } catch (IOException e) {
            LOGGER.warn("Error when reading font file {}: {}! Using default font.", fontFileName, e);
            return loadFont("font");
        } catch (FontFormatException e) {
            LOGGER.warn("Font file {} invalid: {}! Using default font.", fontFileName, e);
            return loadFont("font");
        }
    }

    private String getAllAvailableChars(String charsetName) {
        CharsetEncoder ce = Charset.forName(charsetName).newEncoder();
        StringBuilder result = new StringBuilder();
        for (char c = 0; c < Character.MAX_VALUE; c++) {
            if (ce.canEncode(c)) {
                result.append(c);
            }
        }
        return result.toString();
    }

    private void buildTexture() throws IOException {
        // Get the font metrics for each character for the selected font by using image
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = img.createGraphics();
        g2D.setFont(font);
        FontMetrics fontMetrics = g2D.getFontMetrics();

        String allChars = getAllAvailableChars(charSetName);
        int width = 0;
        int height = 0;
        for (char c : allChars.toCharArray()) {
            // Get the size for each character and update global image size
            CharInfo charInfo = new CharInfo(width, fontMetrics.charWidth(c));
            charMap.put(c, charInfo);
            width += fontMetrics.charWidth(c);
            height = Math.max(height, fontMetrics.getHeight());
        }
        g2D.dispose();
        // Create the image associated to the charset
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2D = img.createGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setFont(font);
        fontMetrics = g2D.getFontMetrics();
        g2D.setColor(Color.WHITE);
        int x = 0;
        for (char c : allChars.toCharArray()) {
            CharInfo charInfo = charMap.get(c);
            g2D.drawString("" + c, x, fontMetrics.getAscent());
            x += charInfo.width();
        }
        g2D.dispose();

        LOGGER.trace("Generating temporary font image with dimensions {}x{}", width, height);
        //ImageIO.write(img, IMAGE_FORMAT, new java.io.File("temp.png"));

        InputStream is;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, IMAGE_FORMAT, out);
            out.flush();
            is = new ByteArrayInputStream(out.toByteArray());
        }

        texture = new Texture(is);
    }

    public CharInfo getCharacterInfo(char character) {
        if (!charMap.containsKey(character)) {
            LOGGER.warn("Trying to get info for a non-existing character '{}'", character);
            return charMap.get('\0');
        }
        return charMap.get(character);
    }

    public Texture getTexture() {
        return texture;
    }
}
