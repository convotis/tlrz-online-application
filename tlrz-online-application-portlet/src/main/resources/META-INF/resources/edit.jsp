<%@include file="/init.jsp" %>

<liferay-portlet:actionURL name="<%=OnlineAppPortletKeys.SAVE_ONLINE_APP_CONFIGS%>"
                           var="configurationActionURL">
</liferay-portlet:actionURL>

<c:if test="${result == 'success'}">
    <div class="alert alert-success">Properties are saved successfully!</div>
</c:if>
<c:if test="${result == 'fail'}">
    <div class="alert alert-error">Properties could not be saved</div>
</c:if>

<aui:form action="<%= configurationActionURL %>" method="post" name="fm">

    <aui:input name="<%= Constants.CMD %>" type="hidden"
               value="<%= Constants.UPDATE %>" />

    <aui:fieldset>

        <aui:input name="toTheOnlineApplicationLink" label="to-the-online-application-link" type="text"
                   value="${toTheOnlineApplicationLink}">
        </aui:input>

        <aui:input name="informationAboutGrantLink" label="information-about-grant-link" type="text"
                   value="${informationAboutGrantLink}">
        </aui:input>

        <aui:input name="privacyInformationLink" label="privacy-information-link" type="text"
                   value="${privacyInformationLink}">
        </aui:input>

    </aui:fieldset>
    <aui:button-row>
        <aui:button type="submit" />
    </aui:button-row>
</aui:form>
