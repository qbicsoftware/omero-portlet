package life.qbic.portal.portlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.ImageRenderer;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import life.qbic.omero.BasicOMEROClient;
import life.qbic.portal.utils.ConfigurationManager;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Entry point for portlet omero-client-portlet. This class derives from {@link QBiCPortletUI},
 * which is found in the {@code portal-utils-lib} library.
 *
 * @see <a href=https://github.com/qbicsoftware/portal-utils-lib>portal-utils-lib</a>
 */
@Theme("mytheme")
@SuppressWarnings("serial")
@Widgetset("life.qbic.portal.portlet.AppWidgetSet")
public class OMEROClientPortlet extends QBiCPortletUI {

  private static final Logger LOG = LogManager.getLogger(OMEROClientPortlet.class);

  ///////////////////////////////

  private HashMap<String, Long> revProjMap;
  private HashMap<String, Long> revDsMap;

  private ConfigurationManager cm = ConfigurationManagerFactory.getInstance();

  //////////////////////////////
  //omero json client

  private String baseURL;
  private HttpClient httpClient;
  private BasicHttpContext httpContext;
  private String requestURL;
  private Map<String, String> serviceURLs;
  private String token;
  private JsonObject ctx;

  private String omeroSessionKey;

  /////////////////////////////


  @Override
  protected Layout getPortletContent(final VaadinRequest request) {

    //////////////////////////////
    //omero json client

    this.baseURL = "http://134.2.183.129/omero/api/v0/";

    this.requestURL = this.baseURL;

    this.httpClient = HttpClients.createDefault();
    this.httpContext = new BasicHttpContext();
    BasicCookieStore cookieStore = new BasicCookieStore();
    cookieStore.clear();
    this.httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

    try {

      ctx = omeroJsonLogin(cm.getOmeroUser(), cm.getOmeroPassword(), 1);

      this.omeroSessionKey = ctx.getString("sessionUuid");

      LOG.info(ctx.toString());

    } catch (Exception e) {
      LOG.error("-->json login fail:");
      LOG.debug(e);

    }

    ///////////////////////

    return displayData(request);
  }


  private Layout displayData(final VaadinRequest request) {

    final VerticalLayout mainLayout = new VerticalLayout();
    mainLayout.setSpacing(true);
    mainLayout.setMargin(true);
    mainLayout.setSizeFull();

    mainLayout.addComponent(this.getImgViewer());

    return mainLayout;
  }

  private Panel getImgViewer() {

    Panel imgViewerPanel = new Panel("Image Viewer");
    imgViewerPanel.setWidth("100%");
    imgViewerPanel.setHeight("100%");

    VerticalLayout panelContent = new VerticalLayout();
    panelContent.setSpacing(true);
    panelContent.setMargin(true);
    panelContent.setWidth("100%");
    panelContent.setHeight("100%");

    VerticalLayout projectLayout = new VerticalLayout();
    projectLayout.setSpacing(true);
    projectLayout.setMargin(false);
    projectLayout.setWidth("100%");
    projectLayout.setHeight("100%");

    HorizontalLayout topPanelLayout = new HorizontalLayout();
    topPanelLayout.setSpacing(true);
    topPanelLayout.setMargin(false);
    topPanelLayout.setWidth("50%");
    topPanelLayout.setHeight("100%");

    ComboBox projectBox = new ComboBox("Select project:");
    projectBox.setInvalidAllowed(false);
    projectBox.setNullSelectionAllowed(false);
    projectBox.setWidth("100%");
    projectBox.setImmediate(true);

    BasicOMEROClient oc = new BasicOMEROClient(cm.getOmeroUser(), cm.getOmeroPassword(),
        cm.getOmeroHostname(), Integer.parseInt(cm.getOmeroPort()));

    oc.connect();

    HashMap<Long, String> projectMap = oc.loadProjects();
    oc.disconnect();

    revProjMap = new HashMap<String, Long>();

    Set set = projectMap.entrySet();
    Iterator iterator = set.iterator();
    while (iterator.hasNext()) {
      Map.Entry entry = (Map.Entry) iterator.next();
      projectBox.addItem(entry.getValue());

      revProjMap.put((String) entry.getValue(), (Long) entry.getKey());
    }

    Label projectLabel = new Label("<br>", ContentMode.HTML);

    Button refreshButton = new Button("Refresh");
    refreshButton.setWidth("100%");

    projectLayout.addComponent(projectBox);
    projectLayout.addComponent(refreshButton);

    topPanelLayout.addComponent(projectLayout);
    topPanelLayout.addComponent(projectLabel);

    panelContent.addComponent(topPanelLayout);

    // Have a horizontal split panel as its root layout
    GridLayout hsplit = new GridLayout(6, 2);
    hsplit.setSpacing(true);
    hsplit.setWidth("100%");
    hsplit.setHeight("600px");

    ///////////////////////
    // sample grid

    IndexedContainer sampleGridContainer = new IndexedContainer();

    sampleGridContainer.addContainerProperty("Code", String.class, "-null");
    sampleGridContainer.addContainerProperty("Name", String.class, "-null");

    Grid sampleGrid = new Grid(sampleGridContainer);
    sampleGrid.setCaption("Samples");
    sampleGrid.setSelectionMode(SelectionMode.SINGLE);
    sampleGrid.setWidth("100%");
    sampleGrid.setHeight("100%");

    /////////////
    //add filters

    // Create a header row to hold column filters
    Grid.HeaderRow filterRow = sampleGrid.appendHeaderRow();
    // Set up a filter for all columns
    for (Object pid : sampleGrid.getContainerDataSource().getContainerPropertyIds()) {

      Grid.HeaderCell cell = filterRow.getCell(pid);

      // Have an input field to use for filter
      TextField filterField = new TextField();
      filterField.setColumns(8);

      // Update filter When the filter input is changed
      filterField.addTextChangeListener(change -> {
        // Can't modify filters so need to replace
        sampleGridContainer.removeContainerFilters(pid);

        // (Re)create the filter if necessary
        if (!change.getText().isEmpty()) {
          sampleGridContainer.addContainerFilter(
              new SimpleStringFilter(pid,
                  change.getText(), true, false));
        }
      });
      cell.setComponent(filterField);
    }

    ///////////////////
    //image grid

    IndexedContainer imageGridContainer = new IndexedContainer();

    imageGridContainer.addContainerProperty("Thumbnail", Resource.class, null);
    imageGridContainer.addContainerProperty("Name", String.class, "");
    imageGridContainer.addContainerProperty("Size (X, Y, Z)", String.class, "");
    imageGridContainer.addContainerProperty("Image Time Points", String.class, "");
    imageGridContainer.addContainerProperty("Channels", String.class, "");
    imageGridContainer.addContainerProperty("Full Image", String.class, "");
    imageGridContainer.addContainerProperty("Metadata", String.class, "");

    Grid imageGrid = new Grid(imageGridContainer);
    imageGrid.setCaption("Images");
    imageGrid.setWidth("100%");
    imageGrid.setHeight("100%");
    imageGrid.setSelectionMode(SelectionMode.NONE);
    imageGrid.setStyleName("gridwithpics100px");

    // Set the renderers
    imageGrid.getColumn("Thumbnail").setRenderer(new ImageRenderer());
    imageGrid.getColumn("Full Image").setRenderer(new HtmlRenderer());

    /////////////
    //add filters

    // Create a header row to hold column filters
    filterRow = imageGrid.appendHeaderRow();
    // Set up a filter for all columns
    for (Object pid : imageGrid.getContainerDataSource().getContainerPropertyIds()) {

      if (pid.toString().equals("Thumbnail") || pid.toString().equals("Full Image") || pid
          .toString().equals("Metadata")) {
        continue;
      }

      Grid.HeaderCell cell = filterRow.getCell(pid);

      // Have an input field to use for filter
      TextField filterField = new TextField();
      filterField.setColumns(8);

      // Update filter When the filter input is changed
      filterField.addTextChangeListener(change -> {
        // Can't modify filters so need to replace
        imageGridContainer.removeContainerFilters(pid);

        // (Re)create the filter if necessary
        if (!change.getText().isEmpty()) {
          imageGridContainer.addContainerFilter(
              new SimpleStringFilter(pid,
                  change.getText(), true, false));
        }
      });
      cell.setComponent(filterField);
    }

    /////////////////////////////////////

    hsplit.addComponent(sampleGrid, 0, 0, 1, 1);
    hsplit.addComponent(imageGrid, 2, 0, 5, 1);

    panelContent.addComponent(hsplit);

    imgViewerPanel.setContent(panelContent);

    ////////////////////////////////////////////////////////////////////////

    projectBox.addValueChangeListener(new ValueChangeListener() {
      public void valueChange(ValueChangeEvent event) {

        if (projectBox.getValue() == null) {
          return;
        }

        imageGrid.getContainerDataSource().removeAllItems();

        String projName = projectBox.getValue().toString();
        long projId = ((Long) revProjMap.get(projName)).longValue();

        oc.connect();

        HashMap<String, String> projInfoMap = oc.getProjectInfo(projId);
        projectLabel.setValue("<b>" + projName + "</b><br>" + projInfoMap.get("desc"));

        HashMap<Long, HashMap<String, String>> datasetList = oc
            .getDatasets(revProjMap.get(projName));
        oc.disconnect();

        sampleGrid.getContainerDataSource().removeAllItems();

        Set dsSet = datasetList.entrySet();
        Iterator dsIt = dsSet.iterator();

        revDsMap = new HashMap<String, Long>();

        while (dsIt.hasNext()) {
          Map.Entry dsEntry = (Map.Entry) dsIt.next();

          HashMap<String, String> datasetInfo = (HashMap<String, String>) dsEntry.getValue();

          sampleGrid.addRow(datasetInfo.get("name"), datasetInfo.get("desc"));

          revDsMap.put(datasetInfo.get("name"), (Long) dsEntry.getKey());
        }
      }
    });

    refreshButton.addClickListener(new Button.ClickListener() {
      public void buttonClick(ClickEvent event) {

        imageGrid.getContainerDataSource().removeAllItems();
        projectLabel.setValue("<br>");
        sampleGrid.getContainerDataSource().removeAllItems();

        projectBox.getContainerDataSource().removeAllItems();
        projectBox.setValue(null);

        oc.connect();
        HashMap<Long, String> projectMap = oc.loadProjects();
        oc.disconnect();

        revProjMap = new HashMap<String, Long>();

        Set set = projectMap.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
          Map.Entry entry = (Map.Entry) iterator.next();
          projectBox.addItem(entry.getValue());

          revProjMap.put((String) entry.getValue(), (Long) entry.getKey());
        }

        Notification.show("Refresh done");
      }
    });

    sampleGrid.addSelectionListener(selectionEvent -> {

      Object selected = ((SingleSelectionModel) sampleGrid.getSelectionModel()).getSelectedRow();

      if (selected != null) {

        String sampleName = (String) sampleGrid.getContainerDataSource().getItem(selected)
            .getItemProperty("Code").getValue();

        oc.connect();
        HashMap<Long, String> imageList = oc.getImages(revDsMap.get(sampleName));

        Set imgSet = imageList.entrySet();
        Iterator imgIt = imgSet.iterator();

        imageGrid.getContainerDataSource().removeAllItems();

        int i = 0;
        while (imgIt.hasNext()) {
          Map.Entry imgEntry = (Map.Entry) imgIt.next();

          HashMap<String, String> imageInfoMap = oc
              .getImageInfo(revDsMap.get(sampleName), (long) imgEntry.getKey());
          String size = imageInfoMap.get("size");
          String tps = imageInfoMap.get("tps");
          String chl = imageInfoMap.get("channels");

          //Add MetadataButton to Vaadin Grid and only pass the Button Label to make it runnable in Vaadin 7 Grid

          ButtonRenderer metadataButton = new ButtonRenderer();
          imageGrid.getColumn("Metadata").setRenderer(metadataButton);

          /* Add ClickListener to MetadataButton for opening SubWindow displaying Metadata as necessary
           */
          metadataButton.addClickListener(new RendererClickListener() {
            @Override
            public void click(RendererClickEvent rendererClickEvent) {

              // Define new Window to display metadata
              Window metaDataSubWindow = new Window("Metadata Subwindow");
              //Sets Subwindow Modality
              metaDataSubWindow.setModal(true);

              VerticalLayout subContent = new VerticalLayout();
              subContent.setMargin(true);
              metaDataSubWindow.setContent(subContent);
              //Initialize Grid Column Properties
              IndexedContainer imageGridContainer = new IndexedContainer();

              imageGridContainer.addContainerProperty("MetadataField1", String.class, "");
              imageGridContainer.addContainerProperty("MetadataField2", String.class, "");
              imageGridContainer.addContainerProperty("MetadataField3", String.class, "");

              // Add Properties to Grid
              Grid metadataGrid = new Grid(imageGridContainer);

              // Define Grid Name, SelectionMode and Style
              metadataGrid.setCaption("Metadata");
              metadataGrid.setSelectionMode(SelectionMode.NONE);
              metadataGrid.setStyleName("gridwithpics100px");

              // Add Grid to Window
              subContent.addComponent(metadataGrid);

              //ToDo Get Metadata from Omero Server and Add Metadata to Grid

              // Center subwindow in the browser window
              metaDataSubWindow.center();

              // Open subwindow in the UI
              addWindow(metaDataSubWindow);

            }
          });

          try {

            ByteArrayInputStream imgThum = oc
                .getThumbnail(revDsMap.get(sampleName), (long) imgEntry.getKey());

            byte[] targetArray = new byte[imgThum.available()];
            imgThum.read(targetArray);
            String metadataButtonLabel = "open";
            String link =
                "<div style=\"display:flex; height:100%; width:100%\"> <div style=\"margin: auto;\">"
                    +
                    "<input type=\"button\" value=\"Open\" onclick=\"window.open('"
                    + "http://134.2.183.129/omero/webclient/img_detail/" + String
                    .valueOf(imgEntry.getKey()) + "/?server=1&bsession=" + this.omeroSessionKey
                    + "', '_blank')\">" +
                    "</div></div>";

            imageGrid.addRow(new ExternalResource(
                    "data:image/jpeg;base64," + Base64.encodeBase64String(targetArray)),
                imgEntry.getValue(), size, tps, chl, link, metadataButtonLabel);


          } catch (Exception e) {
            LOG.error(e);
          }

          i++;

        }

        oc.disconnect();

      }


    });

    ////////////////////////////////////////////

    return imgViewerPanel;

  }

  //////////////////////////////////
  //omero json client

  private JsonStructure get(String urlString, Map<String, String> params)
      throws Exception {
    HttpGet httpGet = null;
    if (params == null || params.isEmpty()) {
      httpGet = new HttpGet(urlString);
    } else {
      URIBuilder builder = new URIBuilder(urlString);
      for (Entry<String, String> e : params.entrySet()) {
        builder.addParameter(e.getKey(), e.getValue());
      }
      httpGet = new HttpGet(builder.build());
    }

    HttpResponse res = httpClient.execute(httpGet);
    try (JsonReader reader = Json.createReader(new BufferedReader(
        new InputStreamReader(res.getEntity().getContent())))) {
      return reader.read();
    }
  }

  private JsonStructure post(String url, Map<String, String> params)
      throws HttpException, ClientProtocolException, IOException {

    HttpPost httpPost = new HttpPost(url);
    if (params != null && !params.isEmpty()) {
      List<NameValuePair> nvps = new ArrayList<NameValuePair>();
      for (Entry<String, String> entry : params.entrySet()) {
        nvps.add(new BasicNameValuePair(entry.getKey(), entry
            .getValue()));
      }
      httpPost.setEntity(new UrlEncodedFormEntity(nvps));
    }
    httpPost.addHeader("X-CSRFToken", this.token);
    HttpResponse res = httpClient.execute(httpPost);
    if (res.getStatusLine().getStatusCode() != 200) {
      throw new HttpException("POST failed. URL: " + url + " Status:"
          + res.getStatusLine());
    }

    try (JsonReader reader = Json.createReader(new BufferedReader(
        new InputStreamReader(res.getEntity().getContent())))) {
      return reader.read();
    }
  }

  public Map<String, String> omeroJsonGetURLs() throws Exception {
    JsonObject json = (JsonObject) get(requestURL, null);

    this.serviceURLs = new HashMap<String, String>();

    for (Entry<String, JsonValue> entry : json.entrySet()) {
      this.serviceURLs.put(entry.getKey(),
          ((JsonString) entry.getValue()).getString());
    }

    return this.serviceURLs;
  }

  private String getCSRFToken() throws Exception {
    String url = serviceURLs.get("url:token");
    JsonObject json = (JsonObject) get(url, null);
    return json.getJsonString("data").getString();
  }

  public JsonObject omeroJsonLogin(String username, String password, int serverId)
      throws Exception {
    // make sure we have all the necessary URLs
    //omeroJsonGetVersion();
    //this.requestURL = "http://134.2.183.129/omero/api/v0/";
    omeroJsonGetURLs();

    // make sure we have a CSRF token
    if (this.token == null) {
      this.token = getCSRFToken();
    }

    String url = serviceURLs.get("url:login");
    Map<String, String> params = new HashMap<String, String>();
    params.put("server", "" + serverId);
    params.put("username", username);
    params.put("password", password);

    try {
      JsonObject response = (JsonObject) post(url, params);
      JsonObject ctx = response.getJsonObject("eventContext");
      return ctx;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}