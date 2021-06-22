package de.xdot.configuration;

import aQute.bnd.annotation.metatype.Meta;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

@ExtendedObjectClassDefinition(
        category = PDFMergingConstants.CATEGORY_KEY,
        scope = ExtendedObjectClassDefinition.Scope.SYSTEM
)
@Meta.OCD(
    id = "de.xdot.configuration.TlrzOnlineApplicationConfiguration",
        localization = "content/Language",
        name = PDFMergingConstants.PDF_MERGING_CONF_NAME
)
public interface TlrzOnlineApplicationConfiguration {

    @Meta.AD(
        name = PDFMergingConstants.TYPE_OF_PDF_MERGE,
        optionLabels = {
            PDFMergingConstants.ZIP_FILE_WITH_PDF_REQUEST_ORIGINAL_UPLOADED_RECIPES_ORIGINAL_UPLOADED_INVOICES,
            PDFMergingConstants.ZIP_FILE_WITH_APPLICATION_RECIPES_AND_INVOICES_COMPILED_TO_PDF,
            PDFMergingConstants.ZIP_FILE_WITH_APPLICATION_RECIPES_AND_INVOICES_COMPILED_TO_PDF_ORIGINAL_UPLOADED_RECIPES_ORIGINAL_UPLOADED_INVOICES,
            PDFMergingConstants.ZIP_FILE_FOR_EKABHI
        },
        optionValues = {
            PDFMergingConstants.ZIP_FILE_WITH_PDF_REQUEST_ORIGINAL_UPLOADED_RECIPES_ORIGINAL_UPLOADED_INVOICES,
            PDFMergingConstants.ZIP_FILE_WITH_APPLICATION_RECIPES_AND_INVOICES_COMPILED_TO_PDF,
            PDFMergingConstants.ZIP_FILE_WITH_APPLICATION_RECIPES_AND_INVOICES_COMPILED_TO_PDF_ORIGINAL_UPLOADED_RECIPES_ORIGINAL_UPLOADED_INVOICES,
            PDFMergingConstants.ZIP_FILE_FOR_EKABHI
        },
        required = false
    )
    public String typeOfPDFMerge();

    @Meta.AD(
        name = PDFMergingConstants.SUBJECT_OF_THE_ERROR_EMAIL,
        required = false
    )
    public String errorEMailSubject();

    @Meta.AD(
        name = PDFMergingConstants.TEXT_OF_THE_ERROR_EMAIL,
        required = false
    )
    public String errorEMailText();

    @Meta.AD(
        name = PDFMergingConstants.EMAIL_RECIPIENT,
        required = false
    )
    public String eMailRecipient();

    @Meta.AD(
        name = PDFMergingConstants.PUBLIC_KEY_TO_ENCRYPT_ZIP_FILE,
        required = false
    )
    public String publicKey();

    @Meta.AD(
        name = PDFMergingConstants.WEB_DAV_LOCATION,
        required = false
    )
    public String webDAVLocation();

    @Meta.AD(
        name = PDFMergingConstants.WEB_DAV_PASSWORD,
        required = false
    )
    public String webDAVPassword();

    @Meta.AD(
        name = PDFMergingConstants.WEB_DAV_USERNAME,
        required = false
    )
    public String webDAVUsername();

    @Meta.AD(
        name = PDFMergingConstants.WEB_DAV_PREEMTIVE_AUTHENTICATION,
        required = false
    )
    public boolean webDAVPreemtiveAuthentication();
}
