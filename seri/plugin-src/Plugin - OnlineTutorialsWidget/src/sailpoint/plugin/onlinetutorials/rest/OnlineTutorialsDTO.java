package sailpoint.plugin.onlinetutorials.rest;

/**
 */
public class OnlineTutorialsDTO {
    private String _description = new String();
    private String _page = new String();
    private String _title_key = new String();

    public String getDescription() {
        return _description;
    }

    public void setDescription(String description ) {
        this._description = description;
    }

    public String getPage() {
        return _page;
    }

    public void setPage(String page) {
        this._page = page;
    }

    public String getTitleKey() {
        return _title_key;
    }

    public void setTitleKey(String titleKey) {
        this._title_key = titleKey;
    }


}

/*
<DashboardContent created="1485372181908" id="40286d0759d713890159d714cd940100" modified="1485372199161" name="Online Tutorials" regionSize="0" source="dashboard/contentFlashBasedTraining.xhtml" title="dash_title_online_tutorials" type="My">
  <Attributes>
    <Map>
      <entry key="tutorials">
        <value>
          <List>
            <Map>
              <entry key="description_key" value="help_tutorial_access_review_identity_description"/>
              <entry key="page" value="manager_access_review/manager_access_review/index.html"/>
              <entry key="title_key" value="help_tutorial_access_review_identity"/>
            </Map>
            <Map>
              <entry key="description_key" value="help_tutorial_access_review_entitlement_description"/>
              <entry key="page" value="entitlement_owner_access_review/entitlement_owner_access_review/index.html"/>
              <entry key="title_key" value="help_tutorial_access_review_entitlement"/>
            </Map>
            <Map>
              <entry key="description_key" value="help_tutorial_home_page_overview_description"/>
              <entry key="page" value="home_page/home_page/index.html"/>
              <entry key="title_key" value="help_tutorial_home_page_overview"/>
            </Map>
            <Map>
              <entry key="description_key" value="help_tutorial_lifecycle_manager_overview_description"/>
              <entry key="page" value="lcm_overview/lcm_overview/index.html"/>
              <entry key="title_key" value="help_tutorial_lifecycle_manager_overview"/>
            </Map>
            <Map>
              <entry key="description_key" value="help_tutorial_individual_access_request_description"/>
              <entry key="page" value="access_request/access_request/index.html"/>
              <entry key="title_key" value="help_tutorial_individual_access_request"/>
            </Map>
          </List>
        </value>
      </entry>
    </Map>
  </Attributes>
  <Description>dash_description_online_tutorials</Description>
</DashboardContent>

*/    
