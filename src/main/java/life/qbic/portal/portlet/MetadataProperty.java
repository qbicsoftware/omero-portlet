package life.qbic.portal.portlet;

import java.util.Objects;

/**
 *  Describes the image metadata properties
 *
 *  This class serves as DTO and contains information about
 *  image metadata properties which were retrieved from the omero server
 *
 * @since 1.1.0
 * @param <T> The type of the contained value

 */
public class MetadataProperty<T> {

  /**
   * Name of the metadata property
   *
   * e.g polarizationOptics
   */
  private final String name;

  /**
   * Value of the metadata property
   *
   * e.g Glen Thompson linear polarizer
   */
  private final T value;

  /**
   * Description of the metadata property
   *
   * e.g Polarization optic hardware
   */
  private final String description;

  public MetadataProperty(String name, T value, String description) {
    this.name = name;
    this.value = value;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public T getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MetadataProperty<?> that = (MetadataProperty<?>) o;
    return name.equals(that.name) &&
        value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    return "MetadataProperty{" +
        "name='" + name + '\'' +
        ", value=" + value +
        '}';
  }
}
