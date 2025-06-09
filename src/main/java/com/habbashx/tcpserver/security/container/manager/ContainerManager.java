package com.habbashx.tcpserver.security.container.manager;

import com.habbashx.tcpserver.security.container.annotation.Container;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Abstract class representing a container manager responsible for handling
 * metadata and file associations for containerized data management.
 * Classes extending this abstract class can utilize annotations to define
 * metadata, such as associated file paths, for persistent storage.
 * <p>
 * The class provides utility methods to retrieve either the annotated container
 * file name or the associated {@link File} instance if the container is annotated
 * with the {@code @Container} annotation```.
 * java
 */
public abstract class ContainerManager {

    /**
     * Indicates whether the current class is annotated with the {@link Container} annotation.
     * This flag is determined during the construction of the class and remains constant
     * throughout its lifecycle.
     * <p>
     * The value is set to {@code true} if the {@code @Container} annotation is present on the
     * class; otherwise, it is set to {@code false}. This variable helps in determining whether
     * metadata and associated file operations (defined by the {@code @Container} annotation) can
     * be applied to the class.
     */
    private final boolean isAnnotated;

    public ContainerManager() {

        this.isAnnotated = this.getClass().isAnnotationPresent(Container.class);
    }

    /**
     * Retrieves the file name or path specified by the {@code @Container} annotation
     * applied to the current class, if the annotation is present. If the current class
     * does not have the {@code @Container} annotation, this method returns {@code null}.
     *
     * @return the file name or path specified in the {@code @Container} annotation,
     * or {@code null} if the annotation is not present.
     */
    public @Nullable String getContainerFileName() {

        return isAnnotated ? this.getClass().getAnnotation(Container.class).file() : null;
    }

    /**
     * Retrieves the container file associated with the current class if it is annotated
     * with the {@code @Container} annotation.
     *
     * The method checks whether the class is annotated with {@code @Container}. If so,
     * it retrieves the file path specified in the annotation and returns it as a
     * {@link File} object. If the class is not annotated, the method returns {@code null}.
     *
     * @return a {@link File} object representing the container file if the class is
     * annotated with {@code @Container}, or {@code null} if no annotation is present.
     */
    public @Nullable File getContainerFile() {

        return isAnnotated ? new File(getContainerFileName()) : null;
    }
}
