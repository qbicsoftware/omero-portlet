package life.qbic.portal.portlet;

import java.util.Objects;

/**
 * This class describes a property of metadata for an image
 * @param <T> The type of the contained value
 *
 */
public class MetadataProperty<T> {
  private final String name;
  private final T value;
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
        ", description='" + description + '\'' +
        '}';
  }
}
