/**
 * Jahia FlexPaper SFW viewer integration
 *
 * @url		http://www.jahia.org/
 * @author	Sergiy Shyrkov <sergiy.shyrkov@jahia.com>
 * @version	1.1
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 */
;(function($) {
	$.fn.docViewer = function(options) {
		var player = (typeof contextJsParameters != 'undefined' && typeof contextJsParameters.contextPath != 'undefined' ? contextJsParameters.contextPath : '') + '/modules/document-viewer/javascript/flexpaper/FlexPaperViewer.swf';
		var allOpts = $.extend({}, $.fn.docViewer.defaults, options);
		
		return this.each(function() {
			var opts = allOpts;
			if (typeof allOpts.SwfFile == 'undefined' && typeof $(this).attr('rel') != 'undefined') {
                swfFile = $(this).attr('rel');
                swfFile = swfFile.replace("'","%27");
                opts = $.extend({}, allOpts, {SwfFile : swfFile});
			}
			if (typeof $(this).data("docViewer") != 'undefined') {
				$(this).data("docViewer").getApi().loadSwf(opts.SwfFile);
			} else {
				$(this).data("docViewer", flashembed(this, {
	            	src: player,
	            	version: [10, 0],
	            	wmode: 'transparent'
	            }, opts));
			}
		});
	}

	$.fn.docViewer.defaults = {
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
    }
})(jQuery);