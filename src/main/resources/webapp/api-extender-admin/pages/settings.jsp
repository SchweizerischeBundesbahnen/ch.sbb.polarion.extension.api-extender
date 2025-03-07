<%@ page import="ch.sbb.polarion.extension.api_extender.util.RolesUtils" %>
<%@ page import="ch.sbb.polarion.extension.generic.rest.model.Version" %>
<%@ page import="ch.sbb.polarion.extension.generic.util.ExtensionInfo" %>
<%@ page import="java.util.Collection" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">

<%! Version version = ExtensionInfo.getInstance().getVersion();%>

<head>
    <title>Hooks: Settings</title>
    <link rel="stylesheet" href="../ui/generic/css/common.css?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">
    <link rel="stylesheet" href="../css/api-extender-admin.css?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>">
    <script type="module" src="../js/modules/settings.js?bundle=<%= version.getBundleBuildTimestampDigitsOnly() %>"></script>
</head>

<body>

<div class="standard-admin-page">
    <h1>Authorization settings</h1>

    <jsp:include page='/common/jsp/notifications.jsp' />

    <div class="input-container">
        <div class="input-block wide">
            <div id="global_roles" class="roles_table">
                <table>
                    <%
                        Collection<String> globalRoles = RolesUtils.getGlobalRoles();
                        out.println("<b>Global Roles</b>");
                        for (String role : globalRoles) {
                            out.println("" +
                                    "<tr>" +
                                    "  <td><input type='checkbox' id='" + role + "' name='" + role + "'></td>" +
                                    "  <td><label for='" + role + "'>" + role + "</label></td>" +
                                    "</tr>");
                        }
                    %>
                </table>
            </div>


            <div id="project_roles" class="roles_table">
                <table>
                    <%
                        Collection<String> projectRoles = RolesUtils.getProjectRoles(request.getParameter("scope"));
                        if (!projectRoles.isEmpty()) {
                            out.println("<b>Project Roles</b>");
                        }
                        for (String role : projectRoles) {
                            out.println("" +
                                    "<tr>" +
                                    "  <td><input type='checkbox' id='" + role + "' name='" + role + "'></td>" +
                                    "  <td><label for='" + role + "'>" + role + "</label></td>" +
                                    "</tr>");
                        }
                    %>
                </table>
            </div>

        </div>
    </div>

    <input id="settings_name" type="hidden" value="<%= request.getParameter("settings_name")%>"/>
    <input id="scope" type="hidden" value="<%= request.getParameter("scope")%>"/>
    <input id="bundle-timestamp" type="hidden" value="<%= ch.sbb.polarion.extension.generic.util.VersionUtils.getVersion().getBundleBuildTimestamp() %>"/>
</div>

<jsp:include page='/common/jsp/buttons.jsp'/>

<div class="standard-admin-page">
    <h2>Quick Help</h2>

    <div class="quick-help-text">
        <h3>Permissions</h3>
        <p>Reading of project custom fields is not restricted.</p>
        <p>Writing of project custom fields can be allowed with selected global and project roles.</p>
        <p>By default only global admin role is allowed.</p>
    </div>
</div>

</body>
</html>
