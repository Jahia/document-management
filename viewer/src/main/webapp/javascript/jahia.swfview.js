/**
 * Jahia FlexPaper SFW viewer integration
 *
 * @url		http://www.jahia.org/
 * @author	Sergiy Shyrkov <sergiy.shyrkov@jahia.com>
 * @version	1.0.0
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 */
$(document).ready(function() {
    jQuery.each($("a.jahia-doc-viewer"), function(index, el) { 
        $(el).flashembed({
            	src: (typeof contextJsParameters != 'undefined' && typeof contextJsParameters.contextPath != 'undefined' ? contextJsParameters.contextPath : '') + '/modules/document-viewer/javascript/flexpaper/FlexPaperViewer.swf',
            	version: [10, 0]
            }, {
	            SwfFile : el.rel,
	            Scale : 0.6, 
	            ZoomTransition : 'easeOut',
	            ZoomTime : 0.5,
	            ZoomInterval : 0.2,
	            FitPageOnLoad : true,
	            FitWidthOnLoad : false,
	            FullScreenAsMaxWindow : false,
	            ProgressiveLoading : false,
	            MinZoomSize : 0.2,
	            MaxZoomSize : 5,
	            SearchMatchAll : false,
	            InitViewMode : 'Portrait',
	            PrintPaperAsBitmap : false,
	             
	            ViewModeToolsVisible : true,
	            ZoomToolsVisible : true,
	            NavToolsVisible : true,
	            CursorToolsVisible : true,
	            SearchToolsVisible : true,
	                
	            localeChain: 'en_US'
        }); 
    });
});
