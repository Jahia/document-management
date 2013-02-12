[condition][]Video thumbnail service is enabled=eval(videoThumbnailService != null && videoThumbnailService.isEnabled());
[consequence][]Create a video thumbnail named "{thumbnailName}" with offset {offset} of size "{size}"=videoThumbnailService.createThumbnail(node, "{thumbnailName}", {offset}, "{size}", drools);
[consequence][]Create a video thumbnail of size "{size}"=videoThumbnailService.createThumbnail(node, "thumbnail", 0, "{size}", drools);
