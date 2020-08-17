package life.qbic.portal.portlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.ComponentRenderer;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.apache.commons.lang.StringUtils;
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

    private HashMap<String, Long> revProjMap = new HashMap<String, Long>();
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
    private BasicOMEROClient omeroClient;
    private final ArrayList<Project> projects;
    /**
     * Contains samples to be displayed
     */
    private final List<Sample> samples;
    private final List<ImageInfo> imageInfos;
    private ComboBox<Project> projectBox;
    private Label projectLabel;
    private Button refreshButton;
    private Grid<Sample> sampleGrid;
    private Grid<ImageInfo> imageInfoGrid;

    public OMEROClientPortlet() {
        projects = new ArrayList<>();
        revProjMap = new HashMap<String, Long>();
        samples = new ArrayList<>();
        imageInfos = new ArrayList<>();
    }

    /////////////////////////////


    @Override
    protected Layout getPortletContent(final VaadinRequest request) {

        //////////////////////////////
        //omero json client

        //FIXME remove hard coded url
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

            LOG.info(ctx.toString());
        } catch (Exception e) {
            LOG.error("Omero json login failed.");
            LOG.debug(e);
            return new VerticalLayout();
        }

        try {
            omeroClient = new BasicOMEROClient(cm.getOmeroUser(), cm.getOmeroPassword(), cm.getOmeroHostname(), Integer.parseInt(cm.getOmeroPort()));
        } catch (Exception e) {
            LOG.error("Unexpected exception during omero client creation.");
            LOG.debug(e);
            return new VerticalLayout();
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

        loadProjects();

        Panel imgViewerPanel = new Panel("Image Viewer");
        imgViewerPanel.setSizeFull();

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

        projectBox = new ComboBox<>("Select project:");
        projectBox.setEmptySelectionAllowed(false);
        projectBox.setWidth("100%");
        projectBox.setDataProvider(new ListDataProvider<>(projects));
        projectBox.setItemCaptionGenerator(Project::getName);

        projectLabel = new Label("<br>", ContentMode.HTML);

        refreshButton = new Button("Refresh");
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

        sampleGrid = new Grid<>();

        ListDataProvider<Sample> sampleListDataProvider = new ListDataProvider<>(samples);
        sampleGrid.setDataProvider(sampleListDataProvider);
        sampleGrid.setSelectionMode(SelectionMode.SINGLE);
        sampleGrid.setCaption("Samples");
        sampleGrid.setSizeFull();


        Column<Sample, String> sampleCodeColumn = sampleGrid.addColumn(Sample::getCode).setCaption("Code");
        Column<Sample, String> sampleNameColumn = sampleGrid.addColumn(Sample::getName).setCaption("Name");

        HeaderRow sampleFilterRow = sampleGrid.appendHeaderRow();

        setupColumnFilter(sampleListDataProvider, sampleCodeColumn, sampleFilterRow);
        setupColumnFilter(sampleListDataProvider, sampleNameColumn, sampleFilterRow);

        ///////////////////
        //image grid
        imageInfoGrid = new Grid<>();
        Column<ImageInfo, Component> imageThumbnailColumn = imageInfoGrid.addColumn(imageInfo -> {
            Resource thumbnailResource = new ExternalResource("data:image/jpeg;base64,"+ Base64.encodeBase64String(imageInfo.getThumbnail()));
            Image thumbnailImage = new Image("",thumbnailResource);
            return (Component) thumbnailImage;
        }).setCaption("Thumbnail");
        Column<ImageInfo, String> imageNameColumn = imageInfoGrid.addColumn(ImageInfo::getName).setCaption("Name");
        Column<ImageInfo, String> imageSizeColumn = imageInfoGrid.addColumn(ImageInfo::getSize).setCaption("Size (X,Y,Z)");
        Column<ImageInfo, String> imageTpsColumn = imageInfoGrid.addColumn(ImageInfo::getTimePoints).setCaption("Image Time Points");
        Column<ImageInfo, String> imageChannelsColumn = imageInfoGrid.addColumn(ImageInfo::getChannels).setCaption("Channels");
        Column<ImageInfo, Component> imageFullColumn = imageInfoGrid.addColumn(imageInfo -> {
            Link fullImageLink = linkToFullImage(imageInfo.getImageId());
            return (Component) fullImageLink;
        }).setCaption("Full Image");
        Column<ImageInfo, Button> imageMetadataColumn = imageInfoGrid.addColumn(imageInfo -> {
            Button metadataButton = new Button("Show Metadata");
            metadataButton.addClickListener(clickEvent -> {
                Window metadataSubWindow = new Window("Metadata Sub-Window");
                VerticalLayout metadataLayout = new VerticalLayout();

                //TODO get metadata and add components
                Collection<MetadataProperty> metadataProperties = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    metadataProperties.add(new MetadataProperty<Integer>("property"+i, i, "example property no."+i));
                }


                Grid<MetadataProperty> metadataGrid = new Grid<>();
                metadataGrid.setDataProvider(new ListDataProvider<MetadataProperty>(metadataProperties));
                metadataGrid.setSelectionMode(SelectionMode.NONE);

                Column<MetadataProperty, String> nameColumn = metadataGrid.addColumn(
                    MetadataProperty::getName).setCaption("Name");
                Column<MetadataProperty, String> valueColumn = metadataGrid.addColumn(metadataProperty -> {
                    return metadataProperty.getValue().toString();
                }).setCaption("Value");
                Column<MetadataProperty, String> descriptionColumn = metadataGrid.addColumn(
                    MetadataProperty::getDescription).setCaption("Description");
                metadataLayout.addComponent(metadataGrid);

                metadataSubWindow.setContent(metadataLayout);
                metadataSubWindow.setModal(true);
                metadataSubWindow.setResizable(false);
                metadataSubWindow.center();
                addWindow(metadataSubWindow);
            });
            return metadataButton;
        }).setCaption("Metadata");

        /////////////
        imageThumbnailColumn.setRenderer(new ComponentRenderer());
        imageFullColumn.setRenderer(new ComponentRenderer());
        imageMetadataColumn.setRenderer(new ComponentRenderer());


        ListDataProvider<ImageInfo> imageListProvider = new ListDataProvider<>(imageInfos);
        imageInfoGrid.setDataProvider(imageListProvider);
        imageInfoGrid.setCaption("Images");
        imageInfoGrid.setSelectionMode(SelectionMode.NONE);
        imageInfoGrid.setSizeFull();
        imageInfoGrid.setStyleName("gridwithpics100px");

        HeaderRow imageFilterRow = imageInfoGrid.appendHeaderRow();

        setupColumnFilter(imageListProvider, imageNameColumn, imageFilterRow);
        setupColumnFilter(imageListProvider, imageSizeColumn, imageFilterRow);
        setupColumnFilter(imageListProvider, imageTpsColumn, imageFilterRow);
        setupColumnFilter(imageListProvider, imageChannelsColumn, imageFilterRow);

        /////////////////////////////////////

        hsplit.addComponent(sampleGrid, 0, 0, 1, 1);
        hsplit.addComponent(imageInfoGrid, 2,0, 5,1);

        panelContent.addComponent(hsplit);

        imgViewerPanel.setContent(panelContent);

        ////////////////////////////////////////////////////////////////////////
        registerListeners();

        return imgViewerPanel;
    }

    private void registerListeners() {
        // Project selection
        projectBox.addSelectionListener(event -> {
            if (event.getSelectedItem().isPresent()) {
                Project selectedProject = event.getSelectedItem().get();
                // update label
                projectLabel.setValue("<b>" + selectedProject.getName() + "</b><br>"
                    + selectedProject.getDescription());
                // clear unrelated samples
                imageInfos.clear();
                samples.clear();
                // load new samples
                //FIXME please remove crazy HashMap stuff
                HashMap<Long, HashMap<String, String>> projectSamples = omeroClient
                    .getDatasets(selectedProject.getId());
                projectSamples.forEach( (sampleId,sampleInfo) -> {
                    String sampleCode = sampleInfo.get("name");
                    String sampleName = sampleInfo.get("desc");
                    Sample sample = new Sample(sampleId, sampleName, sampleCode);
                    samples.add(sample);
                });
                refreshGrid(imageInfoGrid);
                refreshGrid(sampleGrid);

            } else {
                projectLabel.setValue("");
            }
        });

        sampleGrid.addSelectionListener(event -> {
            imageInfos.clear();
            if (event.getFirstSelectedItem().isPresent()) {
                Sample selectedSample = event.getFirstSelectedItem().get();
                //TODO load datasets
                HashMap<Long, String> sampleImageMap = omeroClient.getImages(selectedSample.getId());
                sampleImageMap.forEach( (imageId, ignoredImageName) -> {
                    //FIXME replace crazy hashmap stuff
                    HashMap<String, String> imageInformationMap = omeroClient.getImageInfo(selectedSample.getId(), imageId);

                    byte[] thumbnail = new byte[0];
                    String imageDescription = imageInformationMap.get("desc");
                    String imageName = imageInformationMap.get("name");
                    String imageSize = imageInformationMap.get("size");
                    String imageTimePoints = imageInformationMap.get("tps");
                    String imageChannels = imageInformationMap.get("channels");
                    //FIXME thumbnail image
                    try {
                        ByteArrayInputStream thumbnailInputStream = omeroClient.getThumbnail(selectedSample.getId(), imageId);
                        thumbnail = new byte[thumbnailInputStream.available()];
                        // ignore integer and store in byte array
                        thumbnailInputStream.read(thumbnail);
                        thumbnailInputStream.close();
                    } catch (Exception e) {
                        LOG.error("Could not retrieve thumbnail for image:" + imageId);
                        LOG.debug(e);
                    }
                    ImageInfo imageInfo = new ImageInfo(imageId, imageName, thumbnail, imageSize, imageTimePoints, imageChannels);
                    imageInfos.add(imageInfo);
                });
            } else {
                //remove selected images
                imageInfos.clear();
            }
            refreshGrid(imageInfoGrid);
        });

        refreshButton.addClickListener(event -> {
            imageInfos.clear();
            samples.clear();
            projects.clear();
            projectBox.setSelectedItem(null);
            loadProjects();
            refreshGrid(imageInfoGrid);
            refreshGrid(sampleGrid);
            Notification.show("Refresh was performed.");
        });
    }

    /**
     * Loads projects from omero into {@link OMEROClientPortlet#projects}
     */
    private void loadProjects() {
        projects.clear();
        omeroClient.connect();
        //FIXME remove crazy HashMap stuff
        HashMap<Long, String> projectMap = omeroClient.loadProjects();
        omeroClient.disconnect();

        for (Entry<Long, String> entry : projectMap.entrySet()) {
            revProjMap.put(entry.getValue(), entry.getKey());
            Long projectId = entry.getKey();

            HashMap<String, String> projectInfo = omeroClient.getProjectInfo(projectId);

            Project project = new Project(projectId, projectInfo.get("name"), projectInfo.get("desc"));
            projects.add(project);
        }
    }

    /**
     *
     * @param imageId the image for which a link should be generated
     * @return a vaadin {@link Link} component
     */
    private Link linkToFullImage(long imageId) {
        String omeroUrl = cm.getOmeroHostname();
        String requestUrl = "/omero/webclient/img_detail/" + imageId + "?server=1&bsession=" + this.omeroSessionKey;
        String url = "http://" + omeroUrl + requestUrl;
        Resource fullImage = new ExternalResource(url);
        Link fullImageLink = new Link("Open Image", fullImage);
        fullImageLink.setTargetName("_blank");
        return fullImageLink;
    }

    private void refreshGrid(Grid<?> grid) {
        grid.getDataProvider().refreshAll();
    }

    /**
     * This method creates a TextField to filter a given column
     * @param dataProvider a {@link ListDataProvider} on which the filtering is applied on
     * @param column the column to be filtered
     * @param headerRow a {@link HeaderRow} to the corresponding {@link Grid}
     */
    private <T> void setupColumnFilter(ListDataProvider<T> dataProvider,
        Column<T, String> column, HeaderRow headerRow) {
        TextField filterTextField = new TextField();
        filterTextField.addValueChangeListener(event -> {
            dataProvider.addFilter(element ->
                StringUtils.containsIgnoreCase(column.getValueProvider().apply(element), filterTextField.getValue())
            );
        });
        filterTextField.setValueChangeMode(ValueChangeMode.EAGER);

        headerRow.getCell(column).setComponent(filterTextField);
        filterTextField.setSizeFull();
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