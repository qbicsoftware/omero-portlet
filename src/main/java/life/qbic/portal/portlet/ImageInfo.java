package life.qbic.portal.portlet;

/**
 * A DTO object for information on an image
 * <p>
 * This object acts as a data transfer object to deliver data for the portlet to display.
 *
 */
public class ImageInfo {
  private final long imageId;
  private final String name;
  private final byte[] thumbnail;
  private final String size;
  private final String timePoints;
  private final String channels;

  private ImageInfo() {
    throw new AssertionError("Default constructor of " + ImageInfo.class + " is disabled.");
  }

  public ImageInfo(long imageId, String name, byte[] thumbnail, String size,
      String timePoints, String channels) {
    this.imageId = imageId;
    this.name = name;
    this.thumbnail = thumbnail.clone();
    this.size = size;
    this.timePoints = timePoints;
    this.channels = channels;
  }

  public long getImageId() {
    return imageId;
  }

  public String getName() {
    return name;
  }

  public byte[] getThumbnail() {
    // Be careful! Object.clone works here because we have a primitive type.
    // Non-primitive types would generate a shallow copy.
    return this.thumbnail.clone();
  }

  public String getSize() {
    return size;
  }

  public String getTimePoints() {
    return timePoints;
  }

  public String getChannels() {
    return channels;
  }
}
