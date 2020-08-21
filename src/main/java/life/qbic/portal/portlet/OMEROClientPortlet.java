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
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.ComponentRenderer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import life.qbic.omero.BasicOMEROClient;
import life.qbic.portal.utils.ConfigurationManager;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final ConfigurationManager cm = ConfigurationManagerFactory.getInstance();

    private BasicOMEROClient omeroClient;
    private final List<Project> projects;

    private final List<Sample> samples;
    private final List<ImageInfo> imageInfos;
    private ComboBox<Project> projectBox;
    private Label projectLabel;
    private Button refreshButton;
    private Grid<Sample> sampleGrid;
    private Grid<ImageInfo> imageInfoGrid;

    public OMEROClientPortlet() {
        projects = new ArrayList<>();
        samples = new ArrayList<>();
        imageInfos = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Layout getPortletContent(final VaadinRequest request) {

        try {
            omeroClient = new BasicOMEROClient(cm.getOmeroUser(), cm.getOmeroPassword(), cm.getOmeroHostname(), Integer.parseInt(cm.getOmeroPort()));
        } catch (Exception e) {
            LOG.error("Unexpected exception during omero client creation.");
            LOG.debug(e);
            return new VerticalLayout();
        }

        Layout result;
        try {
            result = displayData();
        } catch (Exception e) {
            LOG.error(e.getMessage());
            LOG.debug(e);
            Notification.show(e.getMessage());
            result = new VerticalLayout();
        }

        return result;
    }

    private Layout displayData() {

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
            return (Component) new Image("",thumbnailResource);
        }).setCaption("Thumbnail");
        Column<ImageInfo, String> imageNameColumn = imageInfoGrid.addColumn(ImageInfo::getName).setCaption("Name");
        Column<ImageInfo, String> imageSizeColumn = imageInfoGrid.addColumn(ImageInfo::getSize).setCaption("Size (X,Y,Z)");
        Column<ImageInfo, String> imageTpsColumn = imageInfoGrid.addColumn(ImageInfo::getTimePoints).setCaption("Image Time Points");
        Column<ImageInfo, String> imageChannelsColumn = imageInfoGrid.addColumn(ImageInfo::getChannels).setCaption("Channels");
        Column<ImageInfo, Component> imageFullColumn = imageInfoGrid.addColumn(imageInfo -> {
            // Exceptions need to be handled here since they are event based and do not bubble up
            try{
                return (Component) linkToFullImage(imageInfo.getImageId());
            } catch (Exception e) {
                LOG.error("Could not generate full image link for imageId: " + imageInfo.getImageId());
                LOG.debug(e);
                Label noFullImageLabel = new Label("Not available.");
                return (Component) noFullImageLabel;
            }
        }).setCaption("Full Image");
        Column<ImageInfo, Component> downloadImageColumn = imageInfoGrid.addColumn(imageInfo -> {
            // Exceptions need to be handled here since they are event based and do not bubble up
            try {
                Link downloadImageLink = linkToImageDownload(imageInfo.getImageId());
                return (Component) downloadImageLink;
            } catch (Exception e) {
                LOG.error("Could not generate link for imageId: "+ imageInfo.getImageId());
                LOG.debug(e);
                Label noDownloadLabel = new Label("No download available.");
                return (Component) noDownloadLabel;
            }
        }).setCaption("Download Image");

        imageThumbnailColumn.setRenderer(new ComponentRenderer());
        imageFullColumn.setRenderer(new ComponentRenderer());
        downloadImageColumn.setRenderer(new ComponentRenderer());


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
                HashMap<Long, String> sampleImageMap = omeroClient.getImages(selectedSample.getId());
                sampleImageMap.forEach( (imageId, ignoredImageName) -> {
                    HashMap<String, String> imageInformationMap = omeroClient.getImageInfo(selectedSample.getId(), imageId);

                    byte[] thumbnail = new byte[0];
                    String imageName = imageInformationMap.get("name");
                    String imageSize = imageInformationMap.get("size");
                    String imageTimePoints = imageInformationMap.get("tps");
                    String imageChannels = imageInformationMap.get("channels");
                    try {
                        ByteArrayInputStream thumbnailInputStream = omeroClient.getThumbnail(selectedSample.getId(), imageId);
                        thumbnail = new byte[thumbnailInputStream.available()];
                        // ignore integer and store in byte array
                        thumbnailInputStream.read(thumbnail);
                        thumbnailInputStream.close();
                    } catch (IOException ioException) {
                        LOG.error("Could not retrieve thumbnail for image:" + imageId);
                        LOG.debug(ioException);
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
        HashMap<Long, String> projectMap = omeroClient.loadProjects();

        for (Entry<Long, String> entry : projectMap.entrySet()) {
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
        String requestUrl = omeroClient.composeImageDetailAddress(imageId);
        Resource fullImage = new ExternalResource(requestUrl);
        Link fullImageLink = new Link("Open Image", fullImage);
        fullImageLink.setTargetName("_blank");
        return fullImageLink;
    }

    /**
     *
     * @param imageId the image for which a download link should be generated
     * @return a vaadin {@link Link} component linking to the download
     */
    private Link linkToImageDownload(long imageId) {
        String downloadLinkAddress = omeroClient.getImageDownloadLink(imageId);
        Resource downloadImageResource = new ExternalResource(downloadLinkAddress);
        Link downloadImageLink = new Link("Download Image", downloadImageResource);
        downloadImageLink.setTargetName("_blank");
        return downloadImageLink;
    }

    private void refreshGrid(Grid<?> grid) {
        grid.getDataProvider().refreshAll();
        grid.setSizeFull();
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
}