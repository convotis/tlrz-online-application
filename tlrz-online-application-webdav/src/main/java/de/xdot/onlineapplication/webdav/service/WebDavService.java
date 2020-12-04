package de.xdot.onlineapplication.webdav.service;

import com.liferay.portal.kernel.module.configuration.ConfigurationException;

import java.io.File;
import java.io.IOException;

public interface WebDavService {

    void uploadFileToWebDav(File fileToUpload, String filename) throws IOException, ConfigurationException;
}
