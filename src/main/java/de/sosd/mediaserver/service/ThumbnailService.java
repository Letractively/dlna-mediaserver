package de.sosd.mediaserver.service;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ThumbnailService {

    @Autowired
    private MediaserverConfiguration cfg;

    public File getFile(final String uuid, final String extension,
            final Integer width, final Integer height) throws IOException {
        return getFile(uuid, extension, width, height, null);
    }

    public File getFile(final String uuid, final String extension,
            final Integer width,
            final Integer height, File source) throws IOException {
        final String previews = this.cfg.getPreviews();
        if (previews == null) {
            throw new FileNotFoundException("No previews configured!");
        }

        File thumbnail = null;

        if (width != null && height != null) {
            thumbnail = new File(previews, uuid + "_" + width + "_" + height
                    + "." + extension);
            if (!thumbnail.exists()) {
                if (source == null) {
                    source = new File(previews, uuid + "." + extension);
                }
                if (!source.exists()) {
                    throw new FileNotFoundException(
                            "Can't create a scaled image from ["
                                    + source.getAbsolutePath()
                                    + "], file does not exist!");
                }
                createScaled(thumbnail, width, height, source);
            }
        } else {
            thumbnail = new File(previews, uuid + "." + extension);
        }

        if (thumbnail != null && thumbnail.exists()) {
            return thumbnail;
        }

        throw new FileNotFoundException("Thumbnail for id " + uuid
                + " not present! [" + thumbnail.getAbsolutePath() + "]");
    }

    private void createScaled(final File target, final int width,
            final int height, final File source) throws IOException {
        final BufferedImage originalImage = ImageIO.read(source);

        final BufferedImage resizedImage = new BufferedImage(width, height,
                originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB
                        : originalImage.getType());
        final Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);

        ImageIO.write(resizedImage, "jpg", target);

    }

}
