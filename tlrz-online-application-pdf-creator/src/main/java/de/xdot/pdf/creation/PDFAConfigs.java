package de.xdot.pdf.creation;

import de.xdot.pdf.creation.service.impl.PDFGeneratorImpl;
import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.pdfa.XMPSchemaPDFAId;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;

import java.io.InputStream;

public class PDFAConfigs {
    public void applyPDFAConfig(PDDocument doc) throws Exception {
        PDDocumentCatalog cat = doc.getDocumentCatalog();
        PDMetadata metadata = new PDMetadata(doc);
        cat.setMetadata(metadata);

        // jempbox version
        XMPMetadata xmp = new XMPMetadata();
        XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(xmp);
        xmp.addSchema(pdfaid);
        pdfaid.setConformance("B");
        pdfaid.setPart(1);
        pdfaid.setAbout("");
        metadata.importXMPMetadata(xmp.asByteArray());

        InputStream colorProfile = PDFGeneratorImpl.class.getResourceAsStream("/sRGB Color Space Profile.ICM");
        PDOutputIntent oi = new PDOutputIntent(doc, colorProfile);
        oi.setInfo("sRGB IEC61966-2.1");
        oi.setOutputCondition("sRGB IEC61966-2.1");
        oi.setOutputConditionIdentifier("sRGB IEC61966-2.1");
        oi.setRegistryName("http://www.color.org");
        cat.addOutputIntent(oi);
    }
}
