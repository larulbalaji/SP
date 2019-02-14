/*
* Custom Javascript functions may be placed here.
* */

Ext.ns('SailPoint',
       'SailPoint.Define',
       'SailPoint.Define.Grid',
       'SailPoint.Define.Grid.Identity');

/*
 * To have a graphical indicator in the identity table view a renderer is needed that
 * can be configured in UIConfig
 * It simply returns the css class that references an image.
 * to ease implementation I just reused the riskIndicator images
 * Do NOT remove the nbsp. If you do so the image won't be displayed anymore
 *
 * to access the renderer use the following line in UIconfig
 * <ColumnConfig dataIndex="inactive" hideable="true" property="inactive" sortProperty="inactive" 
 *     sortable="true" stateId="inactive" renderer="SailPoint.Define.Grid.Identity.renderStatus"/>
 *
 * AR 02/12/2015 added renderStatus 
 */

SailPoint.Define.Grid.Identity.renderStatus = function( value ) {
	if( value ) {			
		return '<div class=\'riskIndicator ri_highest\'/>&nbsp';
	}			
};

