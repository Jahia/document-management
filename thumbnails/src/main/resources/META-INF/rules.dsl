[condition][]Document thumbnail service is enabled=eval(documentThumbnailService != null && documentThumbnailService.isEnabled());
[consequence][]Create a document thumbnail named "{thumbnailName}" of size {size}=documentThumbnailService.createThumbnail(node, "{thumbnailName}", {size}, drools);
[consequence][]Create a document thumbnail of size {size}=documentThumbnailService.createThumbnail(node, "thumbnail", {size}, drools);
