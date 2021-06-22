package de.xdot.pdf.creation.model.sub;

import java.io.File;

public class TaxFileModel {

    private File file;
    private String filename;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "TaxFileModel{" +
            "file=" + file +
            ", filename='" + filename + '\'' +
            '}';
    }
}
