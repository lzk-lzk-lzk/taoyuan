package com.example.peach.modules.qrcode.support;

import com.example.peach.common.config.QrCodeProperties;
import com.example.peach.common.exception.BusinessException;
import com.example.peach.common.utils.StringUtils;
import com.example.peach.modules.variety.entity.FruitVariety;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
// 二维码卡片生成器
public class QrCodeCardGenerator {

    private final QrCodeProperties qrCodeProperties;

    public QrCodeCardGenerator(QrCodeProperties qrCodeProperties) {
        this.qrCodeProperties = qrCodeProperties;
    }

    // 生成二维码卡片图片
    public GenerateResult generate(FruitVariety variety) {
        String targetUrl = resolveTargetUrl(variety);
        try {
            File templateFile = new File(qrCodeProperties.getTemplatePath());
            BufferedImage template = ImageIO.read(templateFile);
            if (template == null) {
                throw new BusinessException("二维码模板图片读取失败");
            }
            BufferedImage canvas = new BufferedImage(template.getWidth(), template.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = canvas.createGraphics();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.drawImage(template, 0, 0, null);
                drawTitle(g2d, safeText(variety.getVarietyName(), "未命名品种"));
                drawIntro(g2d, buildIntro(variety));
                drawTypeValue(g2d, safeText(variety.getCategoryName(), "-"));
                drawAreaValue(g2d, safeText(variety.getDistributionArea(), "-"));
                drawQrImage(g2d, createQrImage(targetUrl));
            } finally {
                g2d.dispose();
            }
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                ImageIO.write(canvas, "png", bos);
                return new GenerateResult(targetUrl, bos.toByteArray());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("生成二维码卡片失败：" + e.getMessage());
        }
    }

    private void drawTitle(Graphics2D g2d, String title) {
        Font font = loadFont(Font.BOLD, 34f);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(title);
        int x = qrCodeProperties.getTitleCenterX() - textWidth / 2;
        g2d.drawString(title, Math.max(x, 30), qrCodeProperties.getTitleY());
    }

    private void drawIntro(Graphics2D g2d, String intro) {
        Font textFont = loadFont(Font.PLAIN, 22f);
        g2d.setColor(Color.BLACK);
        g2d.setFont(textFont);
        FontMetrics metrics = g2d.getFontMetrics(textFont);
        List<String> lines = wrapText(intro, metrics, qrCodeProperties.getIntroMaxWidth(), qrCodeProperties.getIntroMaxRows());
        int y = qrCodeProperties.getIntroY();
        for (String line : lines) {
            g2d.drawString(line, qrCodeProperties.getIntroX(), y);
            y += qrCodeProperties.getIntroLineHeight();
        }
    }

    private void drawTypeValue(Graphics2D g2d, String value) {
        Font valueFont = loadFont(Font.PLAIN, 24f);
        g2d.setColor(Color.BLACK);
        g2d.setFont(valueFont);
        g2d.drawString(value, qrCodeProperties.getTypeValueX(), qrCodeProperties.getTypeValueY());
    }

    private void drawAreaValue(Graphics2D g2d, String value) {
        Font valueFont = loadFont(Font.PLAIN, 24f);
        g2d.setColor(Color.BLACK);
        g2d.setFont(valueFont);
        g2d.drawString(value, qrCodeProperties.getAreaValueX(), qrCodeProperties.getAreaValueY());
    }

    private void drawQrImage(Graphics2D g2d, BufferedImage qrImage) {
        g2d.drawImage(qrImage, qrCodeProperties.getQrX(), qrCodeProperties.getQrY(),
                qrCodeProperties.getQrSize(), qrCodeProperties.getQrSize(), null);
    }

    private BufferedImage createQrImage(String content) throws Exception {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 520, 520, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private String resolveTargetUrl(FruitVariety variety) {
        // 优先使用配置里的详情页地址前缀并拼接品种ID
        if (StringUtils.hasText(qrCodeProperties.getDefaultTargetPrefix())) {
            return qrCodeProperties.getDefaultTargetPrefix() + variety.getId();
        }
        if (StringUtils.hasText(variety.getQrTargetUrl())) {
            return variety.getQrTargetUrl();
        }
        return String.valueOf(variety.getId());
    }

    private String buildIntro(FruitVariety variety) {
        if (StringUtils.hasText(variety.getFruitTraits())) {
            return variety.getFruitTraits();
        }
        if (StringUtils.hasText(variety.getCultivationPoints())) {
            return variety.getCultivationPoints();
        }
        if (StringUtils.hasText(variety.getRemark())) {
            return variety.getRemark();
        }
        return "暂无简介";
    }

    private List<String> wrapText(String text, FontMetrics metrics, int maxWidth, int maxRows) {
        List<String> lines = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            lines.add("暂无简介");
            return lines;
        }
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String candidate = current.toString() + c;
            if (metrics.stringWidth(candidate) > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder().append(c);
                if (lines.size() >= maxRows - 1) {
                    break;
                }
            } else {
                current.append(c);
            }
        }
        if (lines.size() < maxRows && current.length() > 0) {
            lines.add(current.toString());
        }
        if (text.length() > String.join("", lines).length() && !lines.isEmpty()) {
            int lastIndex = lines.size() - 1;
            String last = lines.get(lastIndex);
            lines.set(lastIndex, last.length() > 1 ? last.substring(0, last.length() - 1) + "..." : "...");
        }
        return lines;
    }

    private String safeText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    // 优先读取配置的字体文件，解决 Linux 中文乱码问题
    private Font loadFont(int style, float size) {
        if (StringUtils.hasText(qrCodeProperties.getFontPath())) {
            File fontFile = new File(qrCodeProperties.getFontPath());
            if (fontFile.exists() && fontFile.canRead()) {
                try {
                    return Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(style, size);
                } catch (FontFormatException | IOException e) {
                    log.warn("二维码字体文件加载失败，将使用系统字体回退, path: {}", qrCodeProperties.getFontPath(), e);
                }
            } else {
                log.warn("二维码字体文件不存在或不可读, path: {}", qrCodeProperties.getFontPath());
            }
        }
        return new Font(qrCodeProperties.getFontFamily(), style, Math.round(size));
    }

    public record GenerateResult(String targetUrl, byte[] imageBytes) {
    }
}
