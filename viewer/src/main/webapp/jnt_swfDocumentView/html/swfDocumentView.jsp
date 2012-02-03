<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="docviewer" uri="http://www.jahia.org/tags/docviewer" %>
<c:if test="${renderContext.editMode}">
    <template:addResources type="css" resources="files.css"/>
    <span class="icon ${functions:fileIcon(currentNode.name)}"></span><a href="<c:url value='${currentNode}'/>">${fn:escapeXml(currentNode.name)}</a>
    (<fmt:message key="jnt_swfDocumentView.noPreviewInEditMode"/>)
</c:if>
<c:if test="${!renderContext.editMode}">
<jcr:nodeProperty name="j:node" node="${currentNode}" var="docProperty"/>
<c:set var="doc" value="${not empty docProperty ? docProperty.node : null}"/>
<c:if test="${not empty doc && docviewer:isViewable(doc)}">
    <c:url var="swfUrl" value="${docviewer:getViewUrl(doc, true)}" context="/"/>
    <c:if test="${not empty swfUrl}">
        <template:addResources type="javascript" resources="jquery.min.js,flexpaper/flexpaper_flash.min.js,jahia.swfview.js"/>
        <jcr:nodeProperty name="j:width" node="${currentNode}" var="width"/>
        <jcr:nodeProperty name="j:height" node="${currentNode}" var="height"/>
        <a class="jahia-doc-viewer" rel="${swfUrl}" style="width:${functions:default(width.string, '640')}px; height:${functions:default(height.string, '480')}px; display:block"></a>
    </c:if>
</c:if>
</c:if>