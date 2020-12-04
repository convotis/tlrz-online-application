package de.xdot.pdf.creation.model.sub;

import java.io.File;

public class ApplicantFileModel {
    private File file;
    private String type;
    private String filename;
    private String originalFilename;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    @Override
    public String toString() {
        return "ApplicantFileModel{" +
            "file=" + file +
            ", type='" + type + '\'' +
            ", filename='" + filename + '\'' +
            ", originalFilename='" + originalFilename + '\'' +
            '}';
    }
}
