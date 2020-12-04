package de.xdot.tlrz.online.application.command;

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import de.xdot.tlrz.online.application.constants.OnlineAppPortletKeys;
import org.osgi.service.component.annotations.Component;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

@Component(immediate = true,
           property = {"javax.portlet.name=" + OnlineAppPortletKeys.ONLINEAPP, "mvc.command.name=" + OnlineAppPortletKeys.SAVE_ONLINE_APP_CONFIGS},
           service = MVCActionCommand.class)
public class TlrzOnlineConfigurationAction extends BaseMVCActionCommand {

    @Override
    protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {

        PortletPreferences preferences = actionRequest.getPreferences();
        String saveResult = "success";

        try {
            preferences.setValue("toTheOnlineApplicationLink", ParamUtil.getString(actionRequest, "toTheOnlineApplicationLink"));

            preferences.setValue("informationAboutGrantLink", ParamUtil.getString(actionRequest, "informationAboutGrantLink"));

            preferences.setValue("privacyInformationLink", ParamUtil.getString(actionRequest, "privacyInformationLink"));
        } catch (ReadOnlyException e) {
            saveResult = "fail";
        }
        preferences.store();

        actionRequest.setAttribute("result", saveResult);
    }
}
