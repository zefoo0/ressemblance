package org.zbaudoin.filesimilarity.infrastructure.comparison.image;

import org.zbaudoin.filesimilarity.infrastructure.comparison.strategy.ImageContentComparisonStrategy;
import org.zbaudoin.filesimilarity.infrastructure.config.DefaultAppConfigFactory;
import org.zbaudoin.filesimilarity.infrastructure.persistence.AppConfigRepository;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImageComparisonServiceTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void identicalImagesHaveMaximumSimilarity() throws Exception {
        Path first = createImage("first.png");
        Path second = createImage("second.png");
        ImageComparisonService service = new ImageComparisonService();

        assertEquals(100, service.compare(first.toFile(), second.toFile()));
    }

    @Test
    public void differentImagesHaveLowerSimilarity() throws Exception {
        Path first = createPatternImage("first.png", false);
        Path second = createPatternImage("second.png", true);
        ImageComparisonService service = new ImageComparisonService();

        assertTrue(service.compare(first.toFile(), second.toFile()) < 100);
    }

    @Test
    public void differentColorsLowerSimilarityEvenWhenImageStructureIsTheSame() throws Exception {
        Path first = createSolidImage("first-color.png", Color.RED);
        Path second = createSolidImage("second-color.png", Color.BLUE);
        ImageComparisonService service = new ImageComparisonService();

        assertTrue(service.compare(first.toFile(), second.toFile()) < 100);
    }

    @Test
    public void sameColorsInDifferentRegionsDoNotLookLikeTheSameImage() throws Exception {
        Path first = createQuadrantImage("first-layout.png", new Color[][]{
                {Color.RED, Color.GREEN}, {Color.BLUE, Color.YELLOW}
        });
        Path second = createQuadrantImage("second-layout.png", new Color[][]{
                {Color.BLUE, Color.YELLOW}, {Color.RED, Color.GREEN}
        });
        ImageComparisonService service = new ImageComparisonService();

        assertTrue(service.compare(first.toFile(), second.toFile()) < 70);
    }

    @Test
    public void imageComparatorIsAvailableByDefault() {
        assertTrue(DefaultAppConfigFactory.create().getComparators().stream()
                .anyMatch(ImageContentComparisonStrategy.class::isInstance));
    }

    @Test
    public void imageComparatorIsRegisteredForJsonDeserialization() throws Exception {
        String json = "{\"comparators\":[{\"type\":\"image-content-comparator\",\"extensions\":[\"PNG\"],\"coefficient\":1.0}]}";

        assertTrue(AppConfigRepository.fromJson(json).getComparators().get(0)
                instanceof ImageContentComparisonStrategy);
    }

    private Path createImage(String name) throws Exception {
        return createSolidImage(name, Color.BLUE);
    }

    private Path createSolidImage(String name, Color color) throws Exception {
        Path file = temporaryFolder.newFile(name).toPath();
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
        ImageIO.write(image, "png", file.toFile());
        return file;
    }

    private Path createPatternImage(String name, boolean invert) throws Exception {
        Path file = temporaryFolder.newFile(name).toPath();
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                boolean bright = ((x / 4) + (y / 4)) % 2==0;
                if (invert) {
                    bright = !bright;
                }
                image.setRGB(x, y, bright ? Color.WHITE.getRGB():Color.BLACK.getRGB());
            }
        }
        ImageIO.write(image, "png", file.toFile());
        return file;
    }

    private Path createQuadrantImage(String name, Color[][] colors) throws Exception {
        Path file = temporaryFolder.newFile(name).toPath();
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, colors[y / 32][x / 32].getRGB());
            }
        }
        ImageIO.write(image, "png", file.toFile());
        return file;
    }
}
