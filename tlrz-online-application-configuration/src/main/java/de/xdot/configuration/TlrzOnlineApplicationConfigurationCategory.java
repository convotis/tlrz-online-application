package de.xdot.configuration;

import com.liferay.configuration.admin.category.ConfigurationCategory;
import org.osgi.service.component.annotations.Component;

@Component(service = ConfigurationCategory.class)
public class TlrzOnlineApplicationConfigurationCategory implements ConfigurationCategory {

    private static final String _CATEGORY_KEY = PDFMergingConstants.CATEGORY_KEY;
    private static final String _CATEGORY_SECTION = "platform";

    @Override
    public String getCategoryKey() {
        return _CATEGORY_KEY;
    }

    @Override
    public String getCategorySection() {
        return _CATEGORY_SECTION;
    }
}
