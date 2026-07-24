package com.school.platform.lesson.domain;

import java.net.URI;
import java.util.Set;

public final class BlockConfigurationValidator {
    private static final Set<String> RICH_TEXT_KINDS = Set.of("paragraph", "unordered-list", "ordered-list");
    private static final Set<String> CHART_TYPES = Set.of("line", "bar", "scatter");
    private static final Set<String> DOCUMENT_TYPES = Set.of("NOTE", "DRAWING_BOARD", "GRAPH_BOARD", "FORMULA_SHEET");

    private BlockConfigurationValidator() { }

    public static void validate(BlockType type, BlockConfiguration configuration) {
        if (!matches(type, configuration)) throw new IllegalArgumentException("Configuration does not match block type");
        switch (configuration) {
            case BlockConfiguration.Heading heading -> {
                text(heading.text(), "Heading text", 500);
                if (heading.level() < 1 || heading.level() > 6) fail("Heading level must be 1 through 6");
            }
            case BlockConfiguration.RichText richText -> {
                if (richText.content().isEmpty()) fail("Rich text content cannot be empty");
                richText.content().forEach(element -> {
                    if (!RICH_TEXT_KINDS.contains(element.kind())) fail("Unsupported rich text element");
                    element.spans().forEach(BlockConfigurationValidator::validateSpan);
                    element.items().forEach(item -> item.forEach(BlockConfigurationValidator::validateSpan));
                });
            }
            case BlockConfiguration.Rule ignored -> { }
            case BlockConfiguration.Image image -> {
                text(image.alt(), "Image alt text", 500);
                validateImageUrl(image.url());
            }
            case BlockConfiguration.Formula formula -> text(formula.expression(), "Formula", 2000);
            case BlockConfiguration.Chart chart -> {
                if (!CHART_TYPES.contains(chart.chartType())) fail("Unsupported chart type");
                if (chart.datasets().isEmpty() || chart.datasets().size() > 20) fail("Chart requires 1 to 20 datasets");
                chart.datasets().forEach(dataset -> {
                    text(dataset.label(), "Dataset label", 200);
                    if (dataset.points().isEmpty() || dataset.points().size() > 1000) fail("Dataset requires 1 to 1000 points");
                    dataset.points().forEach(point -> {
                        if (point.x() == null || point.y() == null) fail("Chart points must be numeric");
                    });
                });
            }
            case BlockConfiguration.MultipleChoice choice -> {
                text(choice.prompt(), "Prompt", 2000);
                if (choice.options().size() < 2 || choice.options().size() > 20) fail("Multiple choice requires 2 to 20 options");
                choice.options().forEach(option -> text(option, "Option", 500));
            }
            case BlockConfiguration.NumericAnswer answer -> text(answer.prompt(), "Prompt", 2000);
            case BlockConfiguration.TextAnswer answer -> text(answer.prompt(), "Prompt", 2000);
            case BlockConfiguration.Simulation simulation -> text(simulation.pluginId(), "Plugin ID", 200);
            case BlockConfiguration.WorkspaceLauncher launcher -> {
                text(launcher.label(), "Launcher label", 200);
                if (!DOCUMENT_TYPES.contains(launcher.documentType())) fail("Unsupported workspace document type");
            }
        }
    }

    private static boolean matches(BlockType type, BlockConfiguration configuration) {
        return switch (type) {
            case HEADING -> configuration instanceof BlockConfiguration.Heading;
            case RICH_TEXT -> configuration instanceof BlockConfiguration.RichText;
            case RULE -> configuration instanceof BlockConfiguration.Rule;
            case IMAGE -> configuration instanceof BlockConfiguration.Image;
            case FORMULA -> configuration instanceof BlockConfiguration.Formula;
            case CHART -> configuration instanceof BlockConfiguration.Chart;
            case MULTIPLE_CHOICE -> configuration instanceof BlockConfiguration.MultipleChoice;
            case NUMERIC_ANSWER -> configuration instanceof BlockConfiguration.NumericAnswer;
            case TEXT_ANSWER -> configuration instanceof BlockConfiguration.TextAnswer;
            case SIMULATION -> configuration instanceof BlockConfiguration.Simulation;
            case WORKSPACE_LAUNCHER -> configuration instanceof BlockConfiguration.WorkspaceLauncher;
        };
    }

    private static void validateSpan(BlockConfiguration.TextSpan span) {
        text(span.text(), "Rich text span", 5000);
        if (span.text().contains("<") || span.text().contains(">")) fail("Rich text cannot contain HTML markup");
    }

    private static void validateImageUrl(String value) {
        text(value, "Image URL", 2000);
        URI uri;
        try { uri = URI.create(value); } catch (IllegalArgumentException exception) { fail("Invalid image URL"); return; }
        if (value.startsWith("/assets/")) return;
        if (!Set.of("https", "http").contains(uri.getScheme()) || uri.getHost() == null) fail("Image URL must use HTTP(S) or an asset reference");
    }

    private static void text(String value, String name, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength) fail(name + " is invalid");
    }

    private static void fail(String message) { throw new IllegalArgumentException(message); }
}