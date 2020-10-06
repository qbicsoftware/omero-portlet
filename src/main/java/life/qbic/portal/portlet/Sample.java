package life.qbic.portal.portlet;

import java.util.Objects;

/**
 * A DTO object for information on an imaging sample
 * <p>
 * This object acts as a data transfer object to deliver data for the portlet to display.
 */
public class Sample {
  private final Long id;
  private final String name;
  private final String code;

  private Sample() {
    throw new AssertionError("Default constructor of " + Sample.class + " is disabled.");
  }

  public Sample(Long id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public String getCode() {
    return code;
  }

  public Long getId() {
    return id;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Sample sample = (Sample) o;
    return id.equals(sample.id) &&
        name.equals(sample.name) &&
        code.equals(sample.code);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, code);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "Sample{" +
        "id='" + id + '\'' +
        ", code='" + code + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
