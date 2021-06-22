package de.xdot.onlineapplication.webdav.service.impl;

import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineImpl;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import de.xdot.configuration.TlrzOnlineApplicationConfiguration;
import de.xdot.onlineapplication.webdav.service.WebDavService;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component(service = WebDavService.class, immediate = true)
public class WebDavServiceImpl implements WebDavService {

    private static final Log log = LogFactoryUtil.getLog(WebDavServiceImpl.class);

    private static final int TIMEOUT = 120000;

    @Reference
    private TlrzOnlineApplicationConfiguration onlineApplicationConfiguration;

    private Sardine sardine;

    @Activate
    @Modified
    private void activate() throws ConfigurationException {
        cleanup();

        final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(TIMEOUT)
            .setConnectTimeout(TIMEOUT)
            .setSocketTimeout(TIMEOUT)
            .setContentCompressionEnabled(false)
            .build();

        final HttpClientBuilder builder = HttpClients.custom().setDefaultRequestConfig(requestConfig);

        sardine = new SardineImpl(builder, onlineApplicationConfiguration.webDAVUsername(), onlineApplicationConfiguration.webDAVPassword());

        if (onlineApplicationConfiguration.webDAVPreemtiveAuthentication()) {
            try {
                URI uri = new URI(onlineApplicationConfiguration.webDAVLocation());

                sardine.enablePreemptiveAuthentication(uri.getHost());
            } catch (URISyntaxException e) {
                log.warn("Unable to enable preemptive authentication for " + onlineApplicationConfiguration.webDAVLocation() + ". Fallback to default authentication.");
            }
        }
    }

    @Deactivate
    public void deactivate() {
        cleanup();
    }

    private void cleanup() {
        if(sardine != null) {
            try {
                sardine.shutdown();
            } catch (IOException e) {
                log.error("Failed to shutdown sardine", e);
            }
        }
    }

    @Override
    public void uploadFileToWebDav(File fileToUpload, String filename) throws IOException, ConfigurationException {
        if (log.isDebugEnabled()) {
            log.debug("Uploading " + fileToUpload.getPath() + " as " + filename + " to " + onlineApplicationConfiguration.webDAVLocation());
        }

        sardine.put(onlineApplicationConfiguration.webDAVLocation() + "/" + filename.replaceAll(" ", ""), new FileInputStream(fileToUpload));
    }
}
