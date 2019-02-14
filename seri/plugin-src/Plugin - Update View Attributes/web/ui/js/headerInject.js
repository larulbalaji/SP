jQuery(document).ready(function(){
	var uvaUrl = SailPoint.CONTEXT_PATH + '/plugins/pluginPage.jsf?pn=updateViewAttributes';
	var leftCol=jQuery("div.panel-body>div:eq(0)")
	var rightCol=jQuery("div.panel-body>div:eq(1)")
	var colToAddTo=leftCol;
	if(leftCol.children().length>rightCol.children().length) {
		colToAddTo=rightCol;
	}
	colToAddTo.append('<a href="'+uvaUrl+'" class="list-group-item">'
        +'<i class="fa fa-chevron-right"></i>'
        +'<b>Identity View Attributes</b>'
        +'<br>'
        +'<span class="text-muted small">Configure the attributes displayed on an Identity Cube.</span></a>');

});