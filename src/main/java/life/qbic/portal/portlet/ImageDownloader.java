package life.qbic.portal.portlet;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * Vaadin {@link com.vaadin.ui.Layout} with functionality to download an image
 *
 * @since: 1.1.0
 */
public class ImageDownloader extends VerticalLayout {


  private Button downloadButton;
  private final long IMAGE_ID;
  /**
   * Default constructor.
   */
  public ImageDownloader(long IMAGE_ID) {
    super();
    this.IMAGE_ID = IMAGE_ID;
    init();
    registerListeners();
  }

  /**
   * Constructs a ImageDownloader with the given components. The components are
   * added in the given order.
   *
   * @see com.vaadin.ui.AbstractOrderedLayout#addComponents(Component...)
   *
   * @param children
   *            The components to add.
   */
  public ImageDownloader(long IMAGE_ID, Component... children) {
    super(children);
    this.IMAGE_ID = IMAGE_ID;
    init();
    registerListeners();
  }

  /**
   *
   */
  private void init() {
    this.setSpacing(false);
    this.setMargin(false);
    this.downloadButton = new Button();
    this.downloadButton.setCaption("Download Image");

    this.addComponent(this.downloadButton);
  }

  /**
   * Downloads the image
   *
   * @see ImageDownloader#IMAGE_ID
   */
  private void downloadImage() {
    //TODO implement

  }

  /**
   * This method registers required event listeners
   */
  private void registerListeners() {

    this.downloadButton.addClickListener(new ClickListener() {
      @Override
      public void buttonClick(ClickEvent event) {
        downloadImage();
      }
    });
  }
}
