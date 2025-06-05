package com.habbashx.tcpserver.security.container.manager;

import com.habbashx.tcpserver.security.container.annotation.Container;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public abstract class ContainerManager {

    private final boolean isAnnotated;

    public ContainerManager() {

        this.isAnnotated = this.getClass().isAnnotationPresent(Container.class);
    }

    public @Nullable String getContainerFileName() {

        return isAnnotated ? this.getClass().getAnnotation(Container.class).file() : null;
    }

    public @Nullable File getContainerFile() {

        return isAnnotated ? new File(this.getClass().getAnnotation(Container.class).file()) : null;
    }
}
