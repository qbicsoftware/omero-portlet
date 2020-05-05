package life.qbic.portal.portlet;

import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;

import com.vaadin.data.Property;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.server.*;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import omero.gateway.model.DatasetData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button.ClickEvent;


import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.ImageRenderer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Image;

import life.qbic.portal.utils.PortalUtils;
import life.qbic.portal.Styles;
import life.qbic.portal.Styles.NotificationType;


///////////////////////////////////////
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import omero.gateway.Gateway;
import omero.gateway.LoginCredentials;
import omero.gateway.SecurityContext;
import omero.gateway.facility.BrowseFacility;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ProjectData;
import omero.log.SimpleLogger;

//////////////////////////////////////////
//OMERO-JSON stuff

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;


////////////////////////////////////

import org.apache.commons.codec.binary.Base64;

//////////////////////////////////
//omero json client

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

//////////////////////


import life.qbic.omero.BasicOMEROClient;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

import life.qbic.portal.utils.ConfigurationManager;


/**
 * Entry point for portlet omero-client-portlet. This class derives from {@link QBiCPortletUI}, which is found in the {@code portal-utils-lib} library.
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

        try{

            ctx = omeroJsonLogin(cm.getOmeroUser(), cm.getOmeroPassword(), 1);


            this.omeroSessionKey = ctx.getString("sessionUuid");

            System.out.println(ctx.toString());

        } catch (Exception e) {
            System.out.println("-->json login fail:");
            e.printStackTrace();

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


        BasicOMEROClient oc = new BasicOMEROClient(cm.getOmeroUser(), cm.getOmeroPassword(), cm.getOmeroHostname(), Integer.parseInt(cm.getOmeroPort()));


        oc.connect();

        //System.out.println("+++++++++++++++++++++session id: " + oc.getSessionId());

        HashMap<Long, String> projectMap = oc.loadProjects();
        oc.disconnect();

        revProjMap = new HashMap<String, Long>();

        Set set = projectMap.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            projectBox.addItem(entry.getValue());

            revProjMap.put((String)entry.getValue(), (Long)entry.getKey());
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
        //hsplit.setMargin(true);
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


        //sampleGrid.addColumn("Code", String.class);
        //sampleGrid.addColumn("Name", String.class);

        //sampleGrid.setFrozenColumnCount(1);

        /////////////
        //add filters

        // Create a header row to hold column filters
        Grid.HeaderRow filterRow = sampleGrid.appendHeaderRow();
        // Set up a filter for all columns
        for (Object pid: sampleGrid.getContainerDataSource().getContainerPropertyIds()) {

            Grid.HeaderCell cell = filterRow.getCell(pid);

            // Have an input field to use for filter
            TextField filterField = new TextField();
            filterField.setColumns(8);

            // Update filter When the filter input is changed
            filterField.addTextChangeListener(change -> {
                // Can't modify filters so need to replace
                sampleGridContainer.removeContainerFilters(pid);

                // (Re)create the filter if necessary
                if (! change.getText().isEmpty())
                    sampleGridContainer.addContainerFilter(
                            new SimpleStringFilter(pid,
                                    change.getText(), true, false));
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

        Grid imageGrid = new Grid(imageGridContainer);
        imageGrid.setCaption("Images");
        imageGrid.setWidth("100%");
        imageGrid.setHeight("100%");
        imageGrid.setSelectionMode(SelectionMode.NONE);
        imageGrid.setStyleName("gridwithpics100px");



        // Define columns
//        imageGrid.addColumn("Thumbnail", Resource.class);
//        imageGrid.addColumn("Name", String.class);
//        imageGrid.addColumn("Size (X x Y x Z)", String.class);
//        imageGrid.addColumn("Time Points", String.class);
//        imageGrid.addColumn("Channels", String.class);
//        imageGrid.addColumn("Full Image", String.class);

        // Set the renderers
        imageGrid.getColumn("Thumbnail").setRenderer(new ImageRenderer());
        imageGrid.getColumn("Full Image").setRenderer(new HtmlRenderer());

        /////////////
        //add filters

        // Create a header row to hold column filters
        filterRow = imageGrid.appendHeaderRow();
        // Set up a filter for all columns
        for (Object pid: imageGrid.getContainerDataSource().getContainerPropertyIds()) {

            //System.out.println("//////////////////-----------------" + pid.toString());
            if(pid.toString().equals("Thumbnail") || pid.toString().equals("Full Image")){
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
                if (! change.getText().isEmpty())
                    imageGridContainer.addContainerFilter(
                            new SimpleStringFilter(pid,
                                    change.getText(), true, false));
            });
            cell.setComponent(filterField);
        }

        /////////////////////////////////////

        hsplit.addComponent(sampleGrid, 0, 0, 1, 1);
        hsplit.addComponent(imageGrid, 2,0, 5,1);

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
                long projId = ((Long)revProjMap.get(projName)).longValue();

                oc.connect();

                HashMap<String, String> projInfoMap = oc.getProjectInfo(projId);
                projectLabel.setValue("<b>" + projName + "</b><br>" + projInfoMap.get("desc"));


                HashMap<Long, HashMap<String, String>> datasetList = oc.getDatasets(revProjMap.get(projName));
                oc.disconnect();

                sampleGrid.getContainerDataSource().removeAllItems();

                Set dsSet = datasetList.entrySet();
                Iterator dsIt = dsSet.iterator();

                revDsMap = new HashMap<String, Long>();

                while (dsIt.hasNext()) {
                    Map.Entry dsEntry = (Map.Entry) dsIt.next();

                    HashMap<String, String> datasetInfo = (HashMap<String, String>)dsEntry.getValue();

                    sampleGrid.addRow(datasetInfo.get("name"), datasetInfo.get("desc"));


                    revDsMap.put(datasetInfo.get("name"), (Long)dsEntry.getKey());
                }
        }});

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

                    revProjMap.put((String)entry.getValue(), (Long)entry.getKey());
                }

                Notification.show("Refresh done");
            }
        });

        sampleGrid.addSelectionListener(selectionEvent -> {

            Object selected = ((SingleSelectionModel)sampleGrid.getSelectionModel()).getSelectedRow();

            if (selected != null){

                String sampleName = (String)sampleGrid.getContainerDataSource().getItem(selected).getItemProperty("Code").getValue();

                oc.connect();
                HashMap<Long, String> imageList = oc.getImages(revDsMap.get(sampleName));


                Set imgSet = imageList.entrySet();
                Iterator imgIt = imgSet.iterator();

                imageGrid.getContainerDataSource().removeAllItems();

                int i = 0;
                while (imgIt.hasNext()) {
                    Map.Entry imgEntry = (Map.Entry) imgIt.next();

                    HashMap<String, String> imageInfoMap = oc.getImageInfo(revDsMap.get(sampleName), (long)imgEntry.getKey());
                    //String imageInfoString = "desc: " + imageInfoMap.get("desc") + ", ";
                    String size = imageInfoMap.get("size");
                    String tps  = imageInfoMap.get("tps");
                    String chl  = imageInfoMap.get("channels");

                    try{

                        ByteArrayInputStream imgThum = oc.getThumbnail(revDsMap.get(sampleName), (long)imgEntry.getKey());


                        byte[] targetArray = new byte[imgThum.available()];
                        imgThum.read(targetArray);


                        //String link = "<a href=\"http://134.2.183.129/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/ \" target=\"_blank\" >open</a>";
                        //String link = "<a href=\"http://134.2.183.129/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/?server=1&bsession=" + this.omeroSessionKey + " \" target=\"_blank\" >open</a>";

                        /////////"http://134.2.183.129/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/?server=1&bsession=" + this.omeroSessionKey +

                        //String link = "<input type=\"button\" value=\"Open\" onclick=\"window.open('" + "http://134.2.183.129/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/?server=1&bsession=" + this.omeroSessionKey + "', '_blank')\">";

                        String link = "<div style=\"display:flex; height:100%; width:100%\"> <div style=\"margin: auto;\">" +
                                "<input type=\"button\" value=\"Open\" onclick=\"window.open('" + "http://134.2.183.129/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/?server=1&bsession=" + this.omeroSessionKey + "', '_blank')\">" +
                                "</div></div>";

                        //String link = "<div style=\"display:flex; height:100%; width:100%\"> <div style=\"margin: auto;\">" +
                        //        "<input type=\"button\" value=\"Open\" onclick=\"window.open('" + "http://134.2.24.118/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/', '_blank')\">" +
                        //        "</div></div>";

                        //String link = "<div style=\"display:flex; height:100%; width:100%\"> <div style=\"margin: auto;\">" +
                        //        "<input type=\"button\" value=\"Open\" onclick=\"window.open('" + "http://134.2.24.118/omero/iviewer/?images=" + String.valueOf(imgEntry.getKey()) + "&bsession=" + this.omeroSessionKey + "', '_blank')\">" +
                        //        "</div></div>";


                        //System.out.println("++url: " + "http://134.2.24.118/omero/iviewer/?images=" + String.valueOf(imgEntry.getKey()) + "&bsession=" + this.omeroSessionKey);

                        //System.out.println("++url: " + "http://134.2.24.118/omero/webgateway/img_detail/" + String.valueOf(imgEntry.getKey()) + "/?server=1&bsession=" + this.omeroSessionKey);
                        //System.out.println("++url: " + "http://134.2.24.118/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/?server=1&bsession=" + this.omeroSessionKey);

                        //getUI().getPage().open("http://google.com", "_blank" );



                        imageGrid.addRow(new ExternalResource("data:image/jpeg;base64,"+Base64.encodeBase64String(targetArray)), imgEntry.getValue(), size, tps, chl, link);




                    } catch (Exception e) {
                        System.out.println(e);
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
        if (params == null || params.isEmpty())
            httpGet = new HttpGet(urlString);
        else {
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
        if (res.getStatusLine().getStatusCode() != 200)
            throw new HttpException("POST failed. URL: " + url + " Status:"
                    + res.getStatusLine());

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
        if (this.token == null)
            this.token = getCSRFToken();

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