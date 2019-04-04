package life.qbic.portal.portlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.portlet.PortletContext;
import javax.portlet.PortletSession;

import com.vaadin.data.Property;
import com.vaadin.ui.themes.ValoTheme;
import omero.gateway.model.DatasetData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.WrappedPortletSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Layout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Image;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Table;



import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Grid;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;

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

import com.vaadin.ui.TreeTable;

import life.qbic.omero.BasicOMEROClient;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;


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

    private final String omero_usr = "bio_user_1";
    private final String omero_pwd = "bio_user_1";

    private HashMap<String, Long> revProjMap;
    private HashMap<String, Long> revDsMap;


    //////////////////////////////


    @Override
    protected Layout getPortletContent(final VaadinRequest request) {
        // TODO: remove this method and to your own thing, please

        //return REMOVE_THIS_METHOD_AND_DO_YOUR_OWN_THING_COMMA_PLEASE(request);
        return displayData(request);
    }

    private Layout displayData(final VaadinRequest request) {

//        String usr = "bio_user_1";
//        String pwd = "bio_user_1";
//
//        LOG.info("Generating content ...");
//
//        final VerticalLayout panelContent = new VerticalLayout();
//        panelContent.setSpacing(true);
//        panelContent.setMargin(true);
//
//
//        final StringBuilder builder = new StringBuilder("<h2><b>OMERO Functionality Prototype</b></h2>");
//        builder.append("<br/><h3><b>User:</b> " + usr + "</h3>");
//
//        final Label welcomeLabel = new Label(builder.toString(), ContentMode.HTML);
//
//
//        panelContent.addComponent(welcomeLabel);
//        insertHorizontalSeparator(panelContent);
//
//        LOG.info("Flag_0............................");
//
//        ///////////////////////////////////////////////////////////////////
//
//        BasicOMEROClient oc = new BasicOMEROClient(usr, pwd);
//        oc.connect();
//        HashMap<Long, String> projectMap = oc.loadProjects();
//
//        LOG.info("Flag_1............................");
//        LOG.info(projectMap.toString());
//
////        Set set = projectMap.entrySet();
////        Iterator iterator = set.iterator();
////        StringBuilder strbuilder = new StringBuilder("<b>OMERO Projects:</b>");
////        while(iterator.hasNext()) {
////            Map.Entry mentry = (Map.Entry)iterator.next();
////
////            strbuilder.append("<br>" + mentry.getValue() + " ---- " + "<a href=\"http://134.2.183.129/omero/webclient/?show=project-" + String.valueOf(mentry.getKey()) + "\">link out!!!!***</a>" );
////        }
////mainLayout
////        Label projectLabel = new Label(strbuilder.toString(), ContentMode.HTML);
////        panelContent.addComponent(projectLabel);
//
//
//        TreeTable projTable = new TreeTable("Projects:");
//        projTable.addContainerProperty("Name", String.class, null);
//        projTable.addContainerProperty("ID", Long.class, null);
//        projTable.addContainerProperty("Link", Label.class, null);
//
//        projTable.setWidth("80em");
//        //projTable.setHeight("20em");
//
//        Set set = projectMap.entrySet();
//        Iterator iterator = set.iterator();
//        int i = 0;
//        while (iterator.hasNext()) {
//            Map.Entry entry = (Map.Entry) iterator.next();
//
//            Label link = new Label("<a href=\"http://134.2.183.129/omero/webclient/?show=project-" + String.valueOf(entry.getKey()) + "\" target=\"_blank\" >OMERO link</a>", ContentMode.HTML);
//
//            projTable.addItem(new Object[]{entry.getValue(), entry.getKey(), link}, i);
//
//            HashMap<Long, String> datasetList = oc.getDatasets((long) entry.getKey());
//            int projectTreeId = i;
//            i += 1;
//
//            Set dsSet = datasetList.entrySet();
//            Iterator dsIt = dsSet.iterator();
//
//            while (dsIt.hasNext()) {
//                Map.Entry dsEntry = (Map.Entry) dsIt.next();
//
//                Label dsLink = new Label("<a href=\"http://134.2.183.129/omero/webclient/?show=dataset-" + String.valueOf(dsEntry.getKey()) + "\" target=\"_blank\" >OMERO link</a>", ContentMode.HTML);
//                projTable.addItem(new Object[]{dsEntry.getValue(), dsEntry.getKey(), dsLink}, i);
//                projTable.setParent(i, projectTreeId);
//
//                HashMap<Long, String> imageList = oc.getImages((long) dsEntry.getKey());
//                int datasetTreeId = i;
//                i += 1;
//
//                Set imgSet = imageList.entrySet();
//                Iterator imgIt = imgSet.iterator();
//
//                while (imgIt.hasNext()) {
//                    Map.Entry imgEntry = (Map.Entry) imgIt.next();
//
//                    Label imgLink = new Label("<a href=\"http://134.2.183.129/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/ \" target=\"_blank\" >OMERO link</a>", ContentMode.HTML);
//                    projTable.addItem(new Object[]{imgEntry.getValue(), imgEntry.getKey(), imgLink}, i);
//                    projTable.setParent(i, datasetTreeId);
//
//                    i += 1;
//                }
//
//
//            }
//
//        }
//
//        panelContent.addComponent(projTable);
//
//        ////////////////////////////////////////////////
//        insertHorizontalSeparator(panelContent);
//
//        //HorizontalLayout hLayout = new HorizontalLayout();
//        VerticalLayout hLayout = new VerticalLayout();
//        hLayout.setSpacing(true);
//
//        TextField projectIdField = new TextField("Project ID");
//        TextField datasetNameField = new TextField("Dataset Name");
//        TextField descField = new TextField("Description");
//
//        descField.setWidth("500px");
//        descField.setValue("Generic description");
//
//        final Button createButton = new Button("Create Dataset");
//
//        hLayout.addComponent(projectIdField);
//        hLayout.addComponent(datasetNameField);
//        hLayout.addComponent(descField);
//        hLayout.addComponent(createButton);
//
//        createButton.addClickListener((Button.ClickListener) event -> {
//
//            oc.createDataset(Long.parseLong(projectIdField.getValue()), datasetNameField.getValue(), descField.getValue());
//            Styles.notification("Dataset Creation", "Dataset created!", NotificationType.SUCCESS);
//
//        });
//        panelContent.addComponent(hLayout);
//
//        //////////////////////////////////////////////////////////
//        insertHorizontalSeparator(panelContent);
//
//        hLayout.setSpacing(true);
//
//        TextField projectNameField = new TextField("Project Name");
//        TextField projDescField = new TextField("Description");
//
//        projDescField.setWidth("500px");
//        projDescField.setValue("Generic description");
//
//        final Button createProjButton = new Button("Create Project");
//
//        hLayout.addComponent(projectNameField);
//        hLayout.addComponent(projDescField);
//        hLayout.addComponent(createProjButton);
//
//        createProjButton.addClickListener((Button.ClickListener) event -> {
//
//            oc.createProject(projectNameField.getValue(), projDescField.getValue());
//            Styles.notification("Project Creation", "Not implemented jet...", NotificationType.SUCCESS);
//
//        });
//        panelContent.addComponent(hLayout);
//
//        //////////////////////////////////////////////////////////
//        insertHorizontalSeparator(panelContent);
//
//        hLayout = new VerticalLayout();
//        hLayout.setSpacing(true);
//
//        TextField projectField = new TextField("Project ID");
//        TextField speciesField = new TextField("Species");
//        TextField tissueField = new TextField("Tissue");
//        TextField micField = new TextField("Microscopy type");
//
//        speciesField.setValue("Arabidopsis thaliana");
//        tissueField.setValue("root");
//        micField.setValue("confocal light microscopy");
//
//        final Button addButton = new Button("Add metadata");
//
//        hLayout.addComponent(projectField);
//        hLayout.addComponent(speciesField);
//        hLayout.addComponent(tissueField);
//        hLayout.addComponent(micField);
//        hLayout.addComponent(addButton);
//
//        addButton.addClickListener((Button.ClickListener) event -> {
//
//            //oc.addMapAnnotationToProject(Long.parseLong(projectField.getValue()), "Species", speciesField.getValue());
//            oc.addMapAnnotationToDataset(Long.parseLong(projectField.getValue()), "Species", speciesField.getValue());
//            oc.addMapAnnotationToDataset(Long.parseLong(projectField.getValue()), "Tissue", tissueField.getValue());
//            oc.addMapAnnotationToDataset(Long.parseLong(projectField.getValue()), "Microscopy", micField.getValue());
//
//            Styles.notification("Project modified", "Meta-data added", NotificationType.SUCCESS);
//
//        });
//        panelContent.addComponent(hLayout);
//
//
//        ///////////////////////////////////////////////
//        oc.disconnect();
//
//        ////////////////////////////////////////

        //final Panel mainPanel = new Panel("OMERO Data");
        //mainPanel.setContent(panelContent);

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        //////////////////////////////////////////////////////////////////////////////////////////////////

        //mainLayout.addComponent(mainPanel);

        //TabSheet hoversheet = new TabSheet();
        //hoversheet.setCaption("Framed TabSheet");

        //hoversheet.addStyleName(ValoTheme.TABSHEET_FRAMED);

        //hoversheet.addTab(new Label("Some content"), "Tab 2");

        //hoversheet.addTab(this.getImgViewer(), "Viewer");
        //hoversheet.addTab(getProjectViewer(), "Project viewer");
        //hoversheet.addTab(getProjectCreator(), "Create project");
        //hoversheet.addTab(getDatasetCreator(), "Create Sample Dataset");

        //hoversheet.addTab(mainPanel, "Test Tab");

        //mainLayout.addComponent(hoversheet);
        mainLayout.addComponent(this.getImgViewer());

        return mainLayout;


    }

    private Panel getProjectViewer() {

        Panel viewerPanel = new Panel("Project Viewer");
        VerticalLayout panelContent = new VerticalLayout();
        panelContent.setSpacing(true);
        panelContent.setMargin(true);

        BasicOMEROClient oc = new BasicOMEROClient(omero_usr, omero_pwd);
        oc.connect();
        HashMap<Long, String> projectMap = oc.loadProjects();

        TreeTable projTable = new TreeTable("Projects:");
        projTable.addContainerProperty("Name", String.class, null);
        projTable.addContainerProperty("ID", Long.class, null);
        projTable.addContainerProperty("Link", Label.class, null);
        projTable.addContainerProperty("Info", String.class, null);

        projTable.setWidth("80em");
        //projTable.setHeight("20em");

        Set set = projectMap.entrySet();
        Iterator iterator = set.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();

            Label link = new Label("<a href=\"http://134.2.183.129/omero/webclient/?show=project-" + String.valueOf(entry.getKey()) + "\" target=\"_blank\" >OMERO link</a>", ContentMode.HTML);

            projTable.addItem(new Object[]{entry.getValue(), entry.getKey(), link, ""}, i);

            HashMap<Long, String> datasetList = oc.getDatasets((long) entry.getKey());
            int projectTreeId = i;
            i += 1;

            Set dsSet = datasetList.entrySet();
            Iterator dsIt = dsSet.iterator();

            while (dsIt.hasNext()) {
                Map.Entry dsEntry = (Map.Entry) dsIt.next();

                Label dsLink = new Label("<a href=\"http://134.2.183.129/omero/webclient/?show=dataset-" + String.valueOf(dsEntry.getKey()) + "\" target=\"_blank\" >OMERO link</a>", ContentMode.HTML);
                projTable.addItem(new Object[]{dsEntry.getValue(), dsEntry.getKey(), dsLink, ""}, i);
                projTable.setParent(i, projectTreeId);

                HashMap<Long, String> imageList = oc.getImages((long) dsEntry.getKey());
                int datasetTreeId = i;
                i += 1;

                Set imgSet = imageList.entrySet();
                Iterator imgIt = imgSet.iterator();

                while (imgIt.hasNext()) {
                    Map.Entry imgEntry = (Map.Entry) imgIt.next();

                    Label imgLink = new Label("<a href=\"http://134.2.183.129/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/ \" target=\"_blank\" >OMERO link</a>", ContentMode.HTML);

                    HashMap<String, String> imageInfoMap = oc.getImageInfo((long)dsEntry.getKey(), (long)imgEntry.getKey());
                    String imageInfoString = "desc: " + imageInfoMap.get("desc") + ", ";
                    imageInfoString = imageInfoString + "size: " + imageInfoMap.get("size") + ", ";
                    imageInfoString = imageInfoString + "tps: " + imageInfoMap.get("tps") + ", ";
                    imageInfoString = imageInfoString + "channels: " + imageInfoMap.get("channels");

                    projTable.addItem(new Object[]{imgEntry.getValue(), imgEntry.getKey(), imgLink, imageInfoString}, i);
                    projTable.setParent(i, datasetTreeId);

                    i += 1;
                }


            }

        }

        oc.disconnect();

        panelContent.addComponent(projTable);
        viewerPanel.setContent(panelContent);

        return viewerPanel;
    }

    private Panel getProjectCreator() {

        Panel projCreatorPanel = new Panel("Create Project");

        VerticalLayout panelContent = new VerticalLayout();
        panelContent.setSpacing(true);
        panelContent.setMargin(true);

        TextField projectNameField = new TextField("Project Name");
        TextField projDescField = new TextField("Description");

        projDescField.setWidth("500px");
        projDescField.setValue("Generic description...");

        final Button createProjButton = new Button("Create Project");
        createProjButton.addClickListener((Button.ClickListener) event -> {

            BasicOMEROClient oc = new BasicOMEROClient(omero_usr, omero_pwd);
            oc.connect();

            oc.createProject(projectNameField.getValue(), projDescField.getValue());
            Styles.notification("Project Creation", "project created", NotificationType.SUCCESS);

            oc.disconnect();

        });

        panelContent.addComponent(projectNameField);
        panelContent.addComponent(projDescField);
        panelContent.addComponent(createProjButton);

        projCreatorPanel.setContent(panelContent);

        return projCreatorPanel;
    }

    private Panel getDatasetCreator() {

        Panel dsCreatorPanel = new Panel("Create Dataset");

        VerticalLayout panelContent = new VerticalLayout();
        panelContent.setSpacing(true);
        panelContent.setMargin(true);

        TextField projectIdField = new TextField("Project ID");
        TextField datasetNameField = new TextField("Dataset Name");
        TextField descField = new TextField("Description");

        descField.setWidth("500px");
        descField.setValue("Generic description");


        TextField speciesField = new TextField("Species");
        TextField tissueField = new TextField("Tissue");
        TextField micField = new TextField("Microscopy type");
        speciesField.setValue("Arabidopsis thaliana");
        tissueField.setValue("root");
        micField.setValue("confocal microscopy");


        final Button createButton = new Button("Create Dataset");
        createButton.addClickListener((Button.ClickListener) event -> {

            BasicOMEROClient oc = new BasicOMEROClient(omero_usr, omero_pwd);
            oc.connect();

            long dataset_id = oc.createDataset(Long.parseLong(projectIdField.getValue()), datasetNameField.getValue(), descField.getValue());
            //LOG.info("////////////////////////////////////////////////////************************************* " + String.valueOf(dataset_id));

            // get dataset ID for the following meta-data
            oc.addMapAnnotationToDataset(dataset_id, "Species", speciesField.getValue());
            oc.addMapAnnotationToDataset(dataset_id, "Tissue", tissueField.getValue());
            oc.addMapAnnotationToDataset(dataset_id, "Microscopy", micField.getValue());

            Styles.notification("Dataset Creation", "Dataset created!", NotificationType.SUCCESS);

            oc.disconnect();

        });

        panelContent.addComponent(projectIdField);
        panelContent.addComponent(datasetNameField);
        panelContent.addComponent(descField);
        panelContent.addComponent(speciesField);
        panelContent.addComponent(tissueField);
        panelContent.addComponent(micField);
        panelContent.addComponent(createButton);

        dsCreatorPanel.setContent(panelContent);

        return dsCreatorPanel;
    }

    private void insertHorizontalSeparator(final Layout layout) {
        final Label horizontalSeparator = new Label("<hr width='100%'/>", ContentMode.HTML);
        layout.addComponent(horizontalSeparator);
    }

    private Panel getImgViewer() {

        Panel projCreatorPanel = new Panel("Image Viewer");

        VerticalLayout panelContent = new VerticalLayout();
        panelContent.setSpacing(true);
        panelContent.setMargin(true);

        HorizontalLayout topPanelLayout = new HorizontalLayout();
        topPanelLayout.setSpacing(true);
        topPanelLayout.setMargin(true);

        ComboBox projectBox = new ComboBox("Select project:");
        projectBox.setInvalidAllowed(false);
        projectBox.setNullSelectionAllowed(false);
        projectBox.setWidth("300px");

        BasicOMEROClient oc = new BasicOMEROClient(this.omero_usr, this.omero_pwd);
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

        Label projectLabel = new Label("", ContentMode.HTML);


        topPanelLayout.addComponent(projectBox);
        topPanelLayout.addComponent(projectLabel);

        panelContent.addComponent(topPanelLayout);

        // Have a horizontal split panel as its root layout
        HorizontalLayout hsplit = new HorizontalLayout();
        hsplit.setSpacing(true);
        hsplit.setMargin(true);

        //left grid

        Grid sampleGrid = new Grid();
        sampleGrid.setCaption("Samples");
        sampleGrid.setSelectionMode(SelectionMode.SINGLE);

        sampleGrid.addColumn("Name", String.class);

        ///////////////////

        Table imageGrid = new Table("Images");

        projectBox.addValueChangeListener(new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {

                imageGrid.getContainerDataSource().removeAllItems();

                String projName = projectBox.getValue().toString();
                long projId = ((Long)revProjMap.get(projName)).longValue();

                oc.connect();

                HashMap<String, String> projInfoMap = oc.getProjectInfo(projId);
                projectLabel.setValue("<b>Project:</b> " + projName + "<br>" + projInfoMap.get("desc"));


                HashMap<Long, String> datasetList = oc.getDatasets(revProjMap.get(projName));
                oc.disconnect();

                sampleGrid.getContainerDataSource().removeAllItems();

                Set dsSet = datasetList.entrySet();
                Iterator dsIt = dsSet.iterator();

                revDsMap = new HashMap<String, Long>();

                while (dsIt.hasNext()) {
                    Map.Entry dsEntry = (Map.Entry) dsIt.next();

                    sampleGrid.addRow(dsEntry.getValue());

                    revDsMap.put((String)dsEntry.getValue(), (Long)dsEntry.getKey());
                }
        }});



        // Define some columns
        imageGrid.addContainerProperty("Thumbnail", Image.class, null);
        imageGrid.addContainerProperty("Name", String.class, null);
        imageGrid.addContainerProperty("Size (X x Y x Z)", String.class, null);
        imageGrid.addContainerProperty("Time Points", String.class, null);
        imageGrid.addContainerProperty("Channels", String.class, null);
        imageGrid.addContainerProperty("Link", Label.class, null);




        sampleGrid.addSelectionListener(selectionEvent -> { // Java 8
            // Get selection from the selection model
            Object selected = ((SingleSelectionModel)sampleGrid.getSelectionModel()).getSelectedRow();

            if (selected != null){

                String sampleName = (String)sampleGrid.getContainerDataSource().getItem(selected).getItemProperty("Name").getValue();

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


                    StreamResource.StreamSource sr = null;
                    StreamResource res = null;


                    try{

                        ByteArrayInputStream imgThum = oc.getThumbnail(revDsMap.get(sampleName), (long)imgEntry.getKey());

                        sr = new StreamResource.StreamSource() {
                            public InputStream getStream()
                            {
                                return imgThum;
                            }
                        };

                        res = new StreamResource(sr, "img_0");

                    } catch (Exception e) {
                        System.out.println(e);
                    }

                    Label link = new Label("<a href=\"http://134.2.183.129/omero/webclient/img_detail/" + String.valueOf(imgEntry.getKey()) + "/ \" target=\"_blank\" >OMERO link</a>", ContentMode.HTML);

                    imageGrid.addItem(new Object[] {new Image("", res), imgEntry.getValue(),
                                                    size, tps, chl, link},
                                                    new Integer(i));

                    i++;

                }

                oc.disconnect();

            }


        });

        sampleGrid.setWidth("200px");
        sampleGrid.setHeight("800px");

        hsplit.addComponent(sampleGrid);

        ///////////////////
        imageGrid.setWidth("1200px");
        imageGrid.setHeight("800px");

        hsplit.addComponent(imageGrid);

        panelContent.addComponent(hsplit);


        projCreatorPanel.setContent(panelContent);


        return projCreatorPanel;
    }
}