package life.qbic.portal.portlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.*;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.ComponentRenderer;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import life.qbic.omero.BasicOMEROClient;
import life.qbic.portal.utils.ConfigurationManager;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import omero.gateway.model.MapAnnotationData;
import omero.model.NamedValue;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for the portlet omero-client-portlet.
 *
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

    private Sample selectedSample;
    private HashMap<Long, String> sampleImageMap;

    private Window imageLoadingWindow;
    private Label imageLoadingStatus;
    private ProgressBar imageLoadingBar;
    private Button cancelImageLoadingButton;
    private ImageDataLoadingThread imageLoadingThread;

    private TextArea projectLabel;
    private Label projectStats;
    private Button refreshButton;
    private Grid<Sample> sampleGrid;
    private Grid<ImageInfo> imageInfoGrid;

    private Grid<Project> projectGrid;

    public OMEROClientPortlet() {
        projects = new ArrayList<>();
        samples = new ArrayList<>();
        imageInfos = new ArrayList<>();
        sampleImageMap = new HashMap<>();
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

        //HorizontalLayout topPanelLayout = new HorizontalLayout();
        //GridLayout topPanelLayout = new GridLayout(3, 1);
        GridLayout topPanelLayout = new GridLayout(2, 4);
        topPanelLayout.setSpacing(true);
        topPanelLayout.setMargin(false);
        topPanelLayout.setWidth("100%");
        topPanelLayout.setHeight("100%");

        ////////////
        // project grid
        projectGrid = new Grid<>();

        ListDataProvider<Project> projectListDataProvider = new ListDataProvider<>(projects);
        projectGrid.setDataProvider(projectListDataProvider);
        projectGrid.setSelectionMode(SelectionMode.SINGLE);
        projectGrid.setCaption("Projects");
        projectGrid.setSizeFull();
        projectGrid.setWidth("100%");
        projectGrid.setHeight("300px");

        Column<Project, String> projectCodeColumn = projectGrid.addColumn(Project::getName).setCaption("Project Code");
        Column<Project, String> projectDescColumn = projectGrid.addColumn(Project::getDescription).setCaption("Project Description");
        projectCodeColumn.setWidth(150);
        projectDescColumn.setWidth(700);

        HeaderRow projectFilterRow = projectGrid.appendHeaderRow();

        setupColumnFilter(projectListDataProvider, projectCodeColumn, projectFilterRow);
        setupColumnFilter(projectListDataProvider, projectDescColumn, projectFilterRow);

        //projectDescColumn.setExpandRatio(1);
        ///////////////////

        refreshButton = new Button("Refresh");
        refreshButton.setWidth("100%");

        projectLabel = new TextArea("Description:");
        projectLabel.setReadOnly(true);
        projectLabel.setSizeFull();
        projectLabel.setHeight("300px");

        projectStats = new Label("<b>Project ID: </b>", ContentMode.HTML);

        projectLayout.addComponent(projectGrid);
        projectLayout.addComponent(refreshButton);

        //topPanelLayout.addComponent(projectLayout);
        // //topPanelLayout.setComponentAlignment(projectLayout, Alignment.TOP_LEFT);
        //topPanelLayout.addComponent(projectLabel);
        // //topPanelLayout.setComponentAlignment(projectLabel, Alignment.TOP_LEFT);


        topPanelLayout.addComponent(projectLayout, 0, 0, 0, 3);
        topPanelLayout.addComponent(projectStats, 1, 0, 1, 0);
        topPanelLayout.addComponent(projectLabel, 1, 1, 1, 3);

        panelContent.addComponent(topPanelLayout);

        // Have a horizontal split panel as its root layout
        //GridLayout hsplit = new GridLayout(6, 2);

        HorizontalSplitPanel hsplit = new HorizontalSplitPanel();

        //hsplit.setSpacing(true);
        hsplit.setWidth("100%");
        hsplit.setHeight("100%");

        ///////////////////////
        // sample grid

        sampleGrid = new Grid<>();

        ListDataProvider<Sample> sampleListDataProvider = new ListDataProvider<>(samples);
        sampleGrid.setDataProvider(sampleListDataProvider);
        sampleGrid.setSelectionMode(SelectionMode.SINGLE);
        sampleGrid.setCaption("Samples");
        sampleGrid.setSizeFull();
        sampleGrid.setHeight("800px");

        Column<Sample, String> sampleCodeColumn = sampleGrid.addColumn(Sample::getCode).setCaption("Sample Code");
        Column<Sample, String> sampleNameColumn = sampleGrid.addColumn(Sample::getName).setCaption("Sample Name");

        HeaderRow sampleFilterRow = sampleGrid.appendHeaderRow();

        setupColumnFilter(sampleListDataProvider, sampleCodeColumn, sampleFilterRow);
        setupColumnFilter(sampleListDataProvider, sampleNameColumn, sampleFilterRow);

        sampleNameColumn.setExpandRatio(1);

        ///////////////////
        //image grid
        imageInfoGrid = new Grid<>();
        Column<ImageInfo, Component> imageThumbnailColumn = imageInfoGrid.addColumn(imageInfo -> {
            Resource thumbnailResource = new ExternalResource("data:image/jpeg;base64,"+ Base64.encodeBase64String(imageInfo.getThumbnail()));
            return (Component) new Image("",thumbnailResource);
        }).setCaption("Thumbnail");
        Column<ImageInfo, String> imageNameColumn = imageInfoGrid.addColumn(ImageInfo::getName).setCaption("Image Name");
        Column<ImageInfo, String> imageChannelsColumn = imageInfoGrid.addColumn(ImageInfo::getChannels).setCaption("Channels");

//        Column<ImageInfo, String> imageSizeColumn = imageInfoGrid.addColumn(imageInfo -> {
//            // Exceptions need to be handled here since they are event based and do not bubble up
//            try{
//                return imageInfo.getSize() + " x " + imageInfo.getTimePoints();
//            } catch (Exception e) {
//                LOG.error("Could not generate image size for imageId: " + imageInfo.getImageId());
//                LOG.debug(e);
//                return "Not available";
//            }
//        }).setCaption("Size (X x Y x Z x T)");

        Column<ImageInfo, Component> imageSizeColumn = imageInfoGrid.addColumn(imageInfo -> {
            // Exceptions need to be handled here since they are event based and do not bubble up
            try{
                Label sizeLabel = new Label("<b>Spatial: </b>" + imageInfo.getSize() + "<br>" + "<b>Timepoints: </b>" + imageInfo.getTimePoints(), ContentMode.HTML);
                return (Component) sizeLabel;
            } catch (Exception e) {
                LOG.error("Could not generate image size for imageId: " + imageInfo.getImageId());
                LOG.debug(e);
                Label noContentLabel = new Label("Not available.");
                return (Component) noContentLabel;
            }
        }).setCaption("Size");

        Column<ImageInfo, Component> imageFullColumn = imageInfoGrid.addColumn(imageInfo -> {
            // Exceptions need to be handled here since they are event based and do not bubble up
            try{
                return (Component) imageLinkButton(imageInfo.getImageId()); //linkToFullImage(imageInfo.getImageId());
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
                Button downloadImageButton = downloadImage(imageInfo.getImageId());
                return (Component) downloadImageButton;
            } catch (Exception e) {
                LOG.error("Could not generate link for imageId: "+ imageInfo.getImageId());
                LOG.debug(e);
                Label noDownloadLabel = new Label("No download available.");
                return (Component) noDownloadLabel;
            }
        }).setCaption("Download Image");
        Column<ImageInfo, Component> imageMetadataColumn = imageInfoGrid.addColumn(imageInfo -> {
            // Exceptions need to be handled here since they are event based and do not bubble up
            try {
                return (Component) metadataButton(imageInfo.getImageId());
            }catch (Exception e) {
                LOG.error("Could not create metadata component for imageId: " + imageInfo.getImageId());
                LOG.debug(e);
                Label noMetadataLabel = new Label("");
                return (Component) noMetadataLabel;
            }}).setCaption("Metadata");

        imageThumbnailColumn.setRenderer(new ComponentRenderer());
        imageFullColumn.setRenderer(new ComponentRenderer());
        downloadImageColumn.setRenderer(new ComponentRenderer());
        imageMetadataColumn.setRenderer(new ComponentRenderer());
        imageSizeColumn.setRenderer(new ComponentRenderer());

        ListDataProvider<ImageInfo> imageListProvider = new ListDataProvider<>(imageInfos);
        imageInfoGrid.setDataProvider(imageListProvider);
        imageInfoGrid.setCaption("Images");
        imageInfoGrid.setSelectionMode(SelectionMode.NONE);
        imageInfoGrid.setSizeFull();
        imageInfoGrid.setHeight("800px");
        imageInfoGrid.setStyleName("gridwithpics100px");

        HeaderRow imageFilterRow = imageInfoGrid.appendHeaderRow();

        setupColumnFilter(imageListProvider, imageNameColumn, imageFilterRow);
        //setupColumnFilter(imageListProvider, imageSizeColumn, imageFilterRow);
        //setupColumnFilter(imageListProvider, imageTpsColumn, imageFilterRow);
        setupColumnFilter(imageListProvider, imageChannelsColumn, imageFilterRow);

        // set column display priority
        imageThumbnailColumn.setExpandRatio(1);
        imageSizeColumn.setExpandRatio(1);
        imageFullColumn.setExpandRatio(1);
        downloadImageColumn.setExpandRatio(1);
        imageMetadataColumn.setExpandRatio(1);

        /////////////////////////////////////

        //hsplit.addComponent(sampleGrid, 0, 0, 1, 1);
        //hsplit.addComponent(imageInfoGrid, 2,0, 5,1);

        hsplit.setFirstComponent(sampleGrid);
        hsplit.setSecondComponent(imageInfoGrid);
        // Set the position of the splitter as percentage
        hsplit.setSplitPosition(25, Sizeable.UNITS_PERCENTAGE);

        panelContent.addComponent(hsplit);

        imgViewerPanel.setContent(panelContent);

        //////////////////////////
        // make img loading window
        imageLoadingWindow = new Window("");
        VerticalLayout imageLoadingLayout = new VerticalLayout();

        imageLoadingStatus = new Label("<b>Loading image data...</b>", ContentMode.HTML);
        imageLoadingStatus.setWidth("400px");

        imageLoadingBar = new ProgressBar(0.0f);
        imageLoadingBar.setSizeFull();

        cancelImageLoadingButton = new Button("Cancel");
        cancelImageLoadingButton.setWidth("100%");

        imageLoadingLayout.addComponent(imageLoadingStatus);
        //imageLoadingLayout.setComponentAlignment(imageLoadingStatus, Alignment.TOP_CENTER);

        imageLoadingLayout.addComponent(imageLoadingBar);

        imageLoadingLayout.addComponent(cancelImageLoadingButton);

        imageLoadingWindow.setContent(imageLoadingLayout);

        imageLoadingWindow.setModal(true);
        imageLoadingWindow.setResizable(false);
        imageLoadingWindow.center();
        imageLoadingWindow.setClosable(false);

        ////////////////////////////////////////////////////////////////////////
        registerListeners();

        return imgViewerPanel;
    }

    private void registerListeners() {

        projectGrid.addSelectionListener(event -> {
            if (event.getFirstSelectedItem().isPresent()) {
                Project selectedProject = event.getFirstSelectedItem().get();

                projectLabel.setValue(String.valueOf(selectedProject.getDescription()));

                // clear unrelated samples
                imageInfos.clear();
                samples.clear();
                // load new samples
                HashMap<Long, HashMap<String, String>> projectSamples = omeroClient.getDatasets(selectedProject.getId());
                projectSamples.forEach( (sampleId, sampleInfo) -> {
                    String sampleCode = sampleInfo.get("name");
                    String sampleName = sampleInfo.get("desc");
                    Sample sample = new Sample(sampleId, sampleName, sampleCode);
                    samples.add(sample);
                });

                projectStats.setValue("<b>Project ID: </b>" + selectedProject.getName() + "<br>"
                        + "<b>No. of Samples: </b>" + samples.size() + "<br>");

                refreshGrid(imageInfoGrid);
                refreshGrid(sampleGrid);

            } else {
                projectLabel.setValue("");
            }

        });

        sampleGrid.addSelectionListener(event -> {

            imageInfos.clear();
            sampleImageMap.clear();
            refreshGrid(imageInfoGrid);

            if (event.getFirstSelectedItem().isPresent()) {
                selectedSample = event.getFirstSelectedItem().get();
                sampleImageMap = omeroClient.getImages(selectedSample.getId());

                try {
                    imageLoadingThread = new ImageDataLoadingThread();
                    imageLoadingThread.start();

                    // Enable polling and set frequency to 0.5 seconds
                    UI.getCurrent().setPollInterval(500);

                    addWindow(imageLoadingWindow);
                }
                catch (Exception e)
                {
                    LOG.error("Error during loading window thread");
                    LOG.debug(e);

                    imageLoadingWindow.close();
                }
            } else {
                //remove selected images
                imageInfos.clear();
                sampleImageMap.clear();
            }

            //refreshGrid(imageInfoGrid);
        });

        refreshButton.addClickListener(event -> {
            imageInfos.clear();
            samples.clear();

            // re-connect with omero
            try {
                omeroClient = new BasicOMEROClient(cm.getOmeroUser(), cm.getOmeroPassword(), cm.getOmeroHostname(), Integer.parseInt(cm.getOmeroPort()));
            } catch (Exception e) {
                LOG.error("Unexpected exception during omero client creation.");
                LOG.debug(e);
            }
            //projectBox.setSelectedItem(null);

            loadProjects();
            //projectGrid.setDataProvider(new ListDataProvider<>(projects));
            refreshGrid(projectGrid);
            projectGrid.deselectAll();

            projectLabel.setValue("");
            projectStats.setValue("<b>Project ID: </b>");

            refreshGrid(imageInfoGrid);
            refreshGrid(sampleGrid);


            Notification.show("Refreshed");
        });

        cancelImageLoadingButton.addClickListener(event -> {

            LOG.info("--> Warning: about to interrupt loading thread ...");

            imageLoadingThread.interrupt();

            imageInfos.clear();
            refreshGrid(imageInfoGrid);

            imageLoadingBar.setValue(0.0F);
            imageLoadingBar.setSizeFull();

            imageLoadingStatus.setValue("<b>Loading image data...</b>");
            imageLoadingStatus.setWidth("400px");

            sampleGrid.deselectAll();

            imageLoadingWindow.close();

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
     * Allows the user to download an image in ome.tiff format for a given image id
     *
     * @param imageId the image for which a download link should be generated
     * @return a vaadin {@link Button} component linking to the download
     */
    private Button downloadImage(long imageId) {
        Button downloadButton = new Button("Download");
        downloadButton.setEnabled(false);

        try{
            downloadButton.addClickListener(clickEvent -> {
                try {
                    String imagePath = omeroClient.downloadOmeTiff(imageId);
                    getUI().getPage().open(imagePath,"_blank");
                }
                catch (Exception e)
                {
                    LOG.error("Could not generate path to ome.tiff file for image: " + imageId);
                    LOG.debug(e);
                }
            });
            downloadButton.setEnabled(true);
        }catch(Exception e){
            throw new RuntimeException("",e);
        }
        return downloadButton;
    }

    /**
     * Generates a vaadin Window which displays the metadata information for the given
     * metadataProperties
     *
     * @param metadataProperties the metadataProperties containing the metadata of an image stored on
     *     the omero server
     * @return a vaadin Window
     */

    private Window metadataWindow(Collection<MetadataProperty> metadataProperties)
    {
        Window metadataWindow = new Window("Metadata Properties");
        VerticalLayout metadataLayout = new VerticalLayout();

        Grid<MetadataProperty> metadataGrid = new Grid<>();
        metadataGrid.setDataProvider(new ListDataProvider<MetadataProperty>(metadataProperties));
        metadataGrid.setSelectionMode(SelectionMode.NONE);

        Column<MetadataProperty, String> nameColumn = metadataGrid.addColumn(MetadataProperty::getName).setCaption("Name");
        Column<MetadataProperty, String> valueColumn = metadataGrid.addColumn(metadataProperty -> {
            return metadataProperty.getValue().toString();
        }).setCaption("Value");

        // remove the descriptionColumn, not needed atm
        //Column<MetadataProperty, String> descriptionColumn = metadataGrid.addColumn(MetadataProperty::getDescription).setCaption("Description");

        metadataLayout.addComponent(metadataGrid);

        metadataWindow.setContent(metadataLayout);
        metadataWindow.setModal(true);
        metadataWindow.setResizable(false);
        metadataWindow.center();

        return metadataWindow;
    }

    private Window imageViewerWindow(String requestUrl)
    {
        Window imageWindow = new Window("Image Viewer");
        VerticalLayout imageLayout = new VerticalLayout();
        imageLayout.setSizeFull();

        Resource fullImage = new ExternalResource(requestUrl);

        BrowserFrame browser = new BrowserFrame("", fullImage);
        browser.setSizeFull();
        imageLayout.addComponent(browser);

        imageWindow.setContent(imageLayout);
        imageWindow.setModal(true);
        imageWindow.setResizable(true);
        imageWindow.center();
        imageWindow.setWidth("1500px");
        imageWindow.setHeight("1000px");

        return imageWindow;
    }

    /**
     * Collects and converts the metadata stored on the omero server for a given imageId into a MetadataProperty Object
     *
     * @param imageId the image for which the metadata should be collected
     * @return Collection of MetadataProperty Objects
     */

    private Collection<MetadataProperty> collectMetadata(long imageId) {

        Collection<MetadataProperty> metadataProperties = new ArrayList<>();
        try {
            List metadataList = omeroClient.fetchMapAnnotationDataForImage(imageId);
            for (int i = 0; i < metadataList.size(); i++) {
                MapAnnotationData currentMapAnnotation = (MapAnnotationData) metadataList.get(i);
                List<NamedValue> list = (List<NamedValue>) currentMapAnnotation.getContent();
                for (NamedValue namedValue : list) {
                    String metaDataKey = namedValue.name;
                    String metaDataValue = namedValue.value;
                    metadataProperties.add(new MetadataProperty<String>(metaDataKey, metaDataValue, "None"));
                }
            }

        } catch (Exception e) {
            LOG.error("Could not retrieve metadata for image:" + imageId);
            LOG.debug(e);

        }
        return metadataProperties;
    }


    /**
     * Generates a vaadin Button which opens a Window displaying the metadata information for a given imageId
     *
     * @param imageId the image for which the Button should be generated
     * @return a vaadin Button
     */

    private Button metadataButton(long imageId) {
        Button metadataButton = new Button("Show");
        metadataButton.setEnabled(false);
        Collection<MetadataProperty> metadataProperties;

        metadataProperties = collectMetadata(imageId);
        if (!metadataProperties.isEmpty()) {
            metadataButton.setEnabled(true);
        }
        else {
            return metadataButton;
        }

        metadataButton.addClickListener(clickEvent -> {
            try {
                Window metadataWindow = metadataWindow(metadataProperties);
                addWindow(metadataWindow);
            }
            catch (Exception e)
            {
                LOG.error("Could not generate metadata subwindow for imageId: " + imageId);
                LOG.debug(e);
            }
        });
        return metadataButton;
    }

    private Button imageLinkButton(long imageId) {
        Button linkButton = new Button("View");
        //linkButton.setEnabled(false);

        String requestUrl = omeroClient.composeImageDetailAddress(imageId);
        linkButton.setEnabled(true);

        linkButton.addClickListener(clickEvent -> {
            try {
                Window imageWindow = imageViewerWindow(requestUrl);
                addWindow(imageWindow);
            }
            catch (Exception e)
            {
                LOG.error("Could not generate image viewer subwindow for imageId: " + imageId);
                LOG.debug(e);
            }
        });
        return linkButton;
    }

    private void refreshGrid(Grid<?> grid) {
        grid.getDataProvider().refreshAll();
        //grid.setSizeFull();

    }

    /**
     * This method creates a TextField to filter a given column
     * @param dataProvider a {@link ListDataProvider} on which the filtering is applied on
     * @param column the column to be filtered
     * @param headerRow a {@link HeaderRow} to the corresponding {@link Grid}
     */
    private <T> void setupColumnFilter(ListDataProvider<T> dataProvider, Column<T, String> column, HeaderRow headerRow) {
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

    // A thread to load image data and update progress bar
    class ImageDataLoadingThread extends Thread {
        // Volatile because read in another thread in access()
        volatile int img_count = 0;
        volatile boolean interrupted = false;

        @Override
        public void run() {

            img_count = 0;

            sampleImageMap.forEach( (imageId, ignoredImageName) -> {

                // continue if interrupted
                if(interrupted){
                    LOG.error("--> skipping loading iteration!");
                    return;
                }

                try {
                    HashMap<String, String> imageInformationMap = omeroClient.getImageInfo(selectedSample.getId(), imageId);

                    String imageName = imageInformationMap.get("name");
                    String imageSize = imageInformationMap.get("size");
                    String imageTimePoints = imageInformationMap.get("tps");
                    String imageChannels = imageInformationMap.get("channels");

                    ByteArrayInputStream thumbnailInputStream = omeroClient.getThumbnail(selectedSample.getId(), imageId);
                    byte[] thumbnail = new byte[thumbnailInputStream.available()];
                    // ignore integer and store in byte array
                    thumbnailInputStream.read(thumbnail);
                    thumbnailInputStream.close();

                    ImageInfo imageInfo = new ImageInfo(imageId, imageName, thumbnail, imageSize, imageTimePoints, imageChannels);

                    imageInfos.add(imageInfo);

                    // update progress window
                    img_count += 1;

                } catch (Exception imgException) {

                    interrupted = true;

                    LOG.error("--> loading error for ID: " + imageId);
                    LOG.error("--> error: " + imgException.getMessage() + "<<--");
                    //LOG.debug(imgException);
                    return;
                }

                // Update the UI thread-safely
                // Loading status
                access(new Runnable() {
                    @Override
                    public void run() {
                        double roundOff = Math.round((((float)img_count/sampleImageMap.size())*100) * 100.0) / 100.0;
                        imageLoadingBar.setValue( ((float)img_count / sampleImageMap.size()) );
                        imageLoadingStatus.setValue("<b>Loading image data... " + roundOff + "%</b>" +
                                                    " (" + img_count + " / " + sampleImageMap.size() + ")");
                    }
                });

                // check for interruption
                try {
                    sleep(10); // 1000 for 1 sec.
                } catch (InterruptedException e) {
                    interrupted = true;
                    LOG.error("--> loading loop interrupt!");

                    //imageInfos.clear();
                }

            });

            // LOG.info("-->>thread loop ended<<--");

            // wait a bit after loading finishes
            // and check for interruption
            try {
                sleep(500); // 1000 for 1 sec.
            } catch (InterruptedException e) {
                interrupted = true;
                LOG.error("--> thread tail interrupt!");
                //LOG.debug(e);
            }

            // Finally, reset UI thread-safely
            access(new Runnable() {
                @Override
                public void run() {
                    // Restore the state to initial
                    imageLoadingBar.setValue(0.0F);
                    imageLoadingStatus.setValue("<b>Loading image data...</b>");

                    if(interrupted){
                        LOG.info("--> thread interruption/error cleanup!");
                        //imageInfos.clear();
                    }
                    refreshGrid(imageInfoGrid);

                    imageLoadingWindow.close();

                    // Stop polling
                    UI.getCurrent().setPollInterval(-1);
                }
            });

        }
    }
}