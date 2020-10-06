package life.qbic.portal.portlet;

import java.util.Objects;

/**
 * A DTO object for information on an imaging project
 * <p>
 * This object acts as a data transfer object to deliver data for the portlet to display.
 */
public class Project {

  private final Long id;
  private final String name;
  private final String description;

  private Project() {
    throw new AssertionError("Default constructor of " + Sample.class + " is disabled.");
  }

  public Project(Long id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
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
    Project project = (Project) o;
    return id.equals(project.id) &&
        name.equals(project.name) &&
        description.equals(project.description);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(id, name, description);
  }
}
