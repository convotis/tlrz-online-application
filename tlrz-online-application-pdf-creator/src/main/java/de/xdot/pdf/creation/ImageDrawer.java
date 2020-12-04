package de.xdot.pdf.creation;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.IOException;

public class ImageDrawer {

    private static final float A_4_WIDTH = 595;
    private static final float A_4_HEIGHT = 842;

    public void drawImage(String path, PDDocument doc) throws IOException {
        PDImageXObject pdImage = PDImageXObject.createFromFile(path, doc);
        PDPage newPpage = new PDPage(PDRectangle.A4);
        doc.addPage(newPpage);
        try (PDPageContentStream contents = new PDPageContentStream(doc, newPpage);) {

            float rateX = A_4_WIDTH / pdImage.getWidth();
            float rateY = A_4_HEIGHT / pdImage.getHeight();
            if (rateX > rateY) {
                int width = (int) (pdImage.getWidth() * rateY);
                int height = (int) (pdImage.getHeight() * rateY);
                contents.drawImage(pdImage, 0, A_4_HEIGHT - height, width, height);
            } else {
                int width = (int) (pdImage.getWidth() * rateX);
                int height = (int) (pdImage.getHeight() * rateX);
                contents.drawImage(pdImage, 0, A_4_HEIGHT - height, width, height);
            }

        }
    }
}
