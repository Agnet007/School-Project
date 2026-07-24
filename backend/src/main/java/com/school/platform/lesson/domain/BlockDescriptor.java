package com.school.platform.lesson.domain;

import java.util.Set;

public record BlockDescriptor(String type, Set<String> extensionCapabilities, boolean renderable,
        boolean interactive, boolean assessable, boolean generative, boolean offlineCapable,
        boolean snapshotCapable) {
}