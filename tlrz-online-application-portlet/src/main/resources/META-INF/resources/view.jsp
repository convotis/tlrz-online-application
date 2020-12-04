<%@ page import="com.liferay.portal.util.PropsValues" %>
<%@ include file="/init.jsp" %>


<c:if test="${!themeDisplay.isSignedIn()}">

    <div class="sign-in">
        Bitte <a class="btn-text" data-redirect="false" href="<%=themeDisplay.getURLSignIn()%>"
                 rel="nofollow">melden Sie sich an</a>, um den Beihilfeantrag auszuf&uuml;llen
    </div>

</c:if>

<c:if test="${themeDisplay.isSignedIn()}">

    <tlrz-submit-form-app></tlrz-submit-form-app>

    <aui:script use="aui-base">
        // Here we can pass the portlet's namespace to the Javascript bootstrap method so that
        // it can attach the boostrap Angular component to the above div tag.
        window.tlrzFrontendApp().default(
        '${toTheOnlineApplicationLink}',
        '${informationAboutGrantLink}',
        '${privacyInformationLink}',
        '<liferay-portlet:resourceURL id="<%=OnlineAppPortletKeys.SUBMIT_FORM%>"/>',
        '${currentUser}',
        '${offset}');
    </aui:script>


    <aui:script use="liferay-session">
        Liferay.Session = new Liferay.SessionBase(
        {
        autoExtend: <%= false %>,
        redirectOnExpire: <%= false %>,
        sessionLength: <%= PropsValues.SESSION_TIMEOUT * 60 %>
        }
        );

    </aui:script>
</c:if>

<!--placeholder-start-->
<script src="/o/tlrz-frontend-app/main.5999db72e403c12529f4.js"></script>
<!--placeholder-end-->