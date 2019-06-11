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
import life.qbic.portal.utils.ConfigurationManagerFactory;
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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Button.ClickEvent;


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


    @Override
    protected Layout getPortletContent(final VaadinRequest request) {
        // TODO: remove this method and to your own thing, please

        //return REMOVE_THIS_METHOD_AND_DO_YOUR_OWN_THING_COMMA_PLEASE(request);
        return displayData(request);
    }

    private Layout displayData(final VaadinRequest request) {

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        mainLayout.addComponent(this.getImgViewer());

        return mainLayout;


    }

    private Panel getImgViewer() {

        Panel projCreatorPanel = new Panel("Image Viewer");

        VerticalLayout panelContent = new VerticalLayout();
        panelContent.setSpacing(true);
        panelContent.setMargin(true);

        VerticalLayout projectLayout = new VerticalLayout();
        projectLayout.setSpacing(true);
        projectLayout.setMargin(false);

        HorizontalLayout topPanelLayout = new HorizontalLayout();
        topPanelLayout.setSpacing(true);
        topPanelLayout.setMargin(true);

        ComboBox projectBox = new ComboBox("Select project:");
        projectBox.setInvalidAllowed(false);
        projectBox.setNullSelectionAllowed(false);
        projectBox.setWidth("300px");
        projectBox.setImmediate(true);


        BasicOMEROClient oc = new BasicOMEROClient(cm.getOmeroUser(), cm.getOmeroPassword(), cm.getOmeroHostname(), Integer.parseInt(cm.getOmeroPort()));
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

        Label projectLabel = new Label("<br>", ContentMode.HTML);

        Button refreshButton = new Button("Refresh");

        projectLayout.addComponent(projectBox);
        projectLayout.addComponent(refreshButton);

        topPanelLayout.addComponent(projectLayout);
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

        sampleGrid.addColumn("Code", String.class);
        sampleGrid.addColumn("Name", String.class);

        ///////////////////

        Table imageGrid = new Table("Images");

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

        sampleGrid.setWidth("400px");
        sampleGrid.setHeight("800px");

        hsplit.addComponent(sampleGrid);

        ///////////////////
        imageGrid.setWidth("1000px");
        imageGrid.setHeight("800px");

        hsplit.addComponent(imageGrid);

        panelContent.addComponent(hsplit);


        projCreatorPanel.setContent(panelContent);


        return projCreatorPanel;
    }
}