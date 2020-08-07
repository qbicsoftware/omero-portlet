package life.qbic.portal.portlet;

/**
 * <short description>
 * <p>
 * <detailed description>
 *
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

  @Override
  public String toString() {
    return "Sample{" +
        "id='" + id + '\'' +
        ", code='" + code + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
