package de.xdot.encryptor.service;

import com.liferay.portal.kernel.exception.PortalException;

import java.io.File;
import java.io.IOException;

public interface PGEncryptor {
    /**
     * Method encrypts given file using GnuPG server.
     * It is assumed, GnuPG is running alongside with portal server, encryption is triggered with command-line interaction.
     *
     * @param file
     * @return encrypted File
     * @throws IOException
     */
    File encryptFile(File file) throws IOException, PortalException, InterruptedException;
}
