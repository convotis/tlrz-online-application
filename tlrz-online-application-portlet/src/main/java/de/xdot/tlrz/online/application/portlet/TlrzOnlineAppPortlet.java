package de.xdot.tlrz.online.application.portlet;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import de.xdot.tlrz.online.application.constants.OnlineAppPortletKeys;
import org.osgi.service.component.annotations.Component;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

@Component(immediate = true,
    property = {"com.liferay.portlet.display-category=category.tlrz", "com.liferay.portlet.header-portlet-css=/css/main.css",
        "com.liferay.portlet.instanceable=true", "javax.portlet.display-name=OnlineApp", "javax.portlet.init-param.template-path=/",
        "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.init-param.edit-template=/edit.jsp",
        "javax.portlet.portlet-mode=text/html;view,edit", "javax.portlet.name=" + OnlineAppPortletKeys.ONLINEAPP,
        "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"},
    service = Portlet.class)
public class TlrzOnlineAppPortlet extends MVCPortlet {

    private static final String DEFAULT_SESSION_TIMEOUT_TITLE = "Hinweis";
    private static final String DEFAULT_SESSION_TIMEOUT_TEXT = "Ihre Session ist aufgelaufen. Sie müssen Ihren Antrag erneut starten. Alle Daten müssen erneut eingegeben werden.";
    private static final String DEFAULT_SESSION_TIMEOUT_BUTTON = "Hier gelangen Sie zum neuen Antrag";
    private static final String DEFAULT_SESSION_TIMEOUT_LINK = "https://verwaltung.thueringen.de/ekabhi";

    @Override
    public void doEdit(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {

        getPreferences(renderRequest);
        super.doEdit(renderRequest, renderResponse);
    }

    @Override
    public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {

        getPreferences(renderRequest);

        try {
            User user = PortalUtil.getUser(renderRequest);

            JSONObject currentUser = JSONFactoryUtil.createJSONObject();

            if (Validator.isNotNull(user)) {
                currentUser.put("firstName", user.getFirstName());
                currentUser.put("lastName", user.getLastName());
                long birthday = user.getBirthday().getTime();

                TimeZone timeZone = TimeZone.getDefault();

                long utcBirthday = birthday + timeZone.getOffset(Calendar.DST_OFFSET);

                if (utcBirthday != 0) {

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(user.getBirthday());

                    String day = String.format("%02d", calendar.get(Calendar.DATE));
                    String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
                    String year = String.format("%04d", calendar.get(Calendar.YEAR));

                    JSONObject birthdayObj = JSONFactoryUtil.createJSONObject();
                    birthdayObj.put("day", day);
                    birthdayObj.put("month", month);
                    birthdayObj.put("year", year);

                    currentUser.put("birthday", birthdayObj);
                }
            }

            renderRequest.setAttribute("currentUser", currentUser);

            ThemeDisplay themeDisplay = (ThemeDisplay) renderRequest.getAttribute(WebKeys.THEME_DISPLAY);

            TimeZone timeZone = themeDisplay.getTimeZone();

            String offsetId = timeZone.toZoneId().getRules().getOffset(Instant.now()).getId();

            renderRequest.setAttribute("offset", offsetId);
        } catch (PortalException e) {
            throw new PortletException("Could not retrieve User from request", e);
        }
        super.doView(renderRequest, renderResponse);
    }

    private void getPreferences(PortletRequest request) {

        PortletPreferences portletPreferences = request.getPreferences();

        request.setAttribute("toTheOnlineApplicationLink", portletPreferences.getValue("toTheOnlineApplicationLink", StringPool.BLANK));

        request.setAttribute("informationAboutGrantLink", portletPreferences.getValue("informationAboutGrantLink", StringPool.BLANK));

        request.setAttribute("privacyInformationLink", portletPreferences.getValue("privacyInformationLink", StringPool.BLANK));

        request.setAttribute("sessionTimeoutTitle", portletPreferences.getValue("sessionTimeoutTitle", DEFAULT_SESSION_TIMEOUT_TITLE));

        request.setAttribute("sessionTimeoutText", portletPreferences.getValue("sessionTimeoutText", DEFAULT_SESSION_TIMEOUT_TEXT));

        request.setAttribute("sessionTimeoutButton", portletPreferences.getValue("sessionTimeoutButton", DEFAULT_SESSION_TIMEOUT_BUTTON));

        request.setAttribute("sessionTimeoutLink", portletPreferences.getValue("sessionTimeoutLink", DEFAULT_SESSION_TIMEOUT_LINK));
    }

}
