package org.zbaudoin.ressemblance.service.comparator.image;

import org.junit.Rule;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zbaudoin.ressemblance.model.Comparator;
import org.junit.rules.TemporaryFolder;
import org.zbaudoin.ressemblance.model.Param;
import org.zbaudoin.ressemblance.service.comparator.ContenuImageComparator;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ImageComparatorServiceTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void identicalImagesHaveMaximumSimilarity() throws Exception {
        Path first = createImage("first.png");
        Path second = createImage("second.png");
        ImageComparatorService service = new ImageComparatorService();

        assertEquals(100, service.compare(first.toFile(), second.toFile()));
    }

    @Test
    public void differentImagesHaveLowerSimilarity() throws Exception {
        Path first = createPatternImage("first.png", false);
        Path second = createPatternImage("second.png", true);
        ImageComparatorService service = new ImageComparatorService();

        assertTrue(service.compare(first.toFile(), second.toFile()) < 100);
    }

    @Test
    public void differentColorsLowerSimilarityEvenWhenImageStructureIsTheSame() throws Exception {
        Path first = createSolidImage("first-color.png", Color.RED);
        Path second = createSolidImage("second-color.png", Color.BLUE);
        ImageComparatorService service = new ImageComparatorService();

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
        ImageComparatorService service = new ImageComparatorService();

        assertTrue(service.compare(first.toFile(), second.toFile()) < 70);
    }

    @Test
    public void imageComparatorIsAvailableByDefault() {
        assertTrue(new Param().getComparators().stream()
                .anyMatch(ContenuImageComparator.class::isInstance));
    }

    @Test
    public void imageComparatorIsRegisteredForJsonDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"type\":\"image-content\",\"extensions\":[\"PNG\"],\"coefficient\":1.0}";

        assertTrue(mapper.readValue(json, Comparator.class) instanceof ContenuImageComparator);
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
