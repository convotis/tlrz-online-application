package de.xdot.encryptor.service.impl;

import com.liferay.portal.kernel.exception.PortalException;
import de.xdot.configuration.TlrzOnlineApplicationConfiguration;
import de.xdot.encryptor.service.PGEncryptor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.File;
import java.io.IOException;


@Component(service = PGEncryptor.class, immediate = true)
public class PGEncryptorImpl implements PGEncryptor {

    private final static String PGP_ENCRYPT_COMMAND_TEMPLATE = "gpg --trust-model always -er %s %s";
    private final static String PGP_EXTENTION = ".gpg";

    @Reference
    private TlrzOnlineApplicationConfiguration onlineApplicationConfiguration;


    @Override
    public File encryptFile(File file) throws IOException, PortalException, InterruptedException {

        Runtime.getRuntime().exec(String.format(PGP_ENCRYPT_COMMAND_TEMPLATE, onlineApplicationConfiguration.publicKey(), file.getAbsolutePath())).waitFor();

        File encryptedFile = new File(file.getAbsolutePath() + PGP_EXTENTION);

        if (encryptedFile.exists()) {
            return encryptedFile;
        } else {
            throw new PortalException(String.format("Error during PGP encryption of the file %s", file.getPath()));
        }
    }
}
