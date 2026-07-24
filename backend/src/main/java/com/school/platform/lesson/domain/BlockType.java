package com.school.platform.lesson.domain;

import java.util.Set;

public enum BlockType {
    HEADING(true, false, false, false, true, false),
    RICH_TEXT(true, false, false, false, true, false),
    RULE(true, false, false, false, true, false),
    IMAGE(true, false, false, false, true, false),
    FORMULA(true, false, false, false, true, false),
    CHART(true, true, false, false, true, true),
    MULTIPLE_CHOICE(true, true, true, false, true, false),
    NUMERIC_ANSWER(true, true, true, false, true, false),
    TEXT_ANSWER(true, true, true, false, true, false),
    SIMULATION(true, true, false, false, false, true),
    WORKSPACE_LAUNCHER(true, true, false, false, false, true);

    private final BlockDescriptor descriptor;

    BlockType(boolean renderable, boolean interactive, boolean assessable, boolean generative,
            boolean offlineCapable, boolean snapshotCapable) {
        this.descriptor = new BlockDescriptor(name(), Set.of(), renderable, interactive, assessable,
                generative, offlineCapable, snapshotCapable);
    }

    public BlockDescriptor descriptor() {
        return descriptor;
    }
}