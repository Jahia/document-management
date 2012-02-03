<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="docviewer" uri="http://www.jahia.org/tags/docviewer" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<c:if test="${renderContext.editMode}">
    <template:addResources type="css" resources="files.css"/>
    <span class="icon ${functions:fileIcon(currentNode.name)}"></span><a href="<c:url value='${currentNode}'/>">${fn:escapeXml(currentNode.name)}</a>
    (<fmt:message key="jnt_swfDocumentView.noPreviewInEditMode"/>)
</c:if>
<c:if test="${!renderContext.editMode}">
    <c:url var="swfUrl" value="${docviewer:getViewUrl(currentNode, true)}" context="/"/>
    <c:if test="${not empty swfUrl}">
        <a class="jahia-doc-viewer" rel="${swfUrl}" style="width:640px; height:480px; display:block"></a>
        <template:addResources type="javascript" resources="jquery.min.js,flexpaper/flexpaper_flash.min.js,jahia.swfview.js" />
    </c:if>
</c:if>