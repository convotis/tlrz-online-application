package de.xdot.onlineapplication.webdav.service.impl;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import de.xdot.configuration.TlrzOnlineApplicationConfiguration;
import de.xdot.onlineapplication.webdav.service.WebDavService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Component(service = WebDavService.class, immediate = true)
public class WebDavServiceImpl implements WebDavService {

    @Reference
    private TlrzOnlineApplicationConfiguration onlineApplicationConfiguration;


    private Sardine sardine;

    @Activate
    private void activate() throws ConfigurationException {

        sardine = SardineFactory.begin(onlineApplicationConfiguration.webDAVUsername(), onlineApplicationConfiguration.webDAVPassword());

    }

    @Override
    public void uploadFileToWebDav(File fileToUpload, String filename) throws IOException, ConfigurationException {
        sardine.put(onlineApplicationConfiguration.webDAVLocation() + "/" + filename.replaceAll(" ", ""), new FileInputStream(fileToUpload));
    }
}
