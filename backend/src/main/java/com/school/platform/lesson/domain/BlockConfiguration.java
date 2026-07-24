package com.school.platform.lesson.domain;

import java.math.BigDecimal;
import java.util.List;

public sealed interface BlockConfiguration permits BlockConfiguration.Heading, BlockConfiguration.RichText,
        BlockConfiguration.Rule, BlockConfiguration.Image, BlockConfiguration.Formula,
        BlockConfiguration.Chart, BlockConfiguration.MultipleChoice, BlockConfiguration.NumericAnswer,
        BlockConfiguration.TextAnswer, BlockConfiguration.Simulation, BlockConfiguration.WorkspaceLauncher {

    record Heading(String text, int level) implements BlockConfiguration { }
    record RichText(List<RichTextElement> content) implements BlockConfiguration {
        public RichText { content = List.copyOf(content); }
    }
    record RichTextElement(String kind, List<TextSpan> spans, List<List<TextSpan>> items) {
        public RichTextElement {
            spans = spans == null ? List.of() : List.copyOf(spans);
            items = items == null ? List.of() : items.stream().map(List::copyOf).toList();
        }
    }
    record TextSpan(String text, boolean bold, boolean italic, boolean inlineCode) { }
    record Rule() implements BlockConfiguration { }
    record Image(String url, String alt, String caption) implements BlockConfiguration { }
    record Formula(String expression, boolean displayMode) implements BlockConfiguration { }
    record Chart(String chartType, String title, String xAxisLabel, String yAxisLabel,
            List<ChartDataset> datasets, boolean tooltip) implements BlockConfiguration {
        public Chart { datasets = List.copyOf(datasets); }
    }
    record ChartDataset(String label, String color, List<ChartPoint> points) {
        public ChartDataset { points = List.copyOf(points); }
    }
    record ChartPoint(BigDecimal x, BigDecimal y) { }
    record MultipleChoice(String prompt, List<String> options, boolean allowMultiple) implements BlockConfiguration {
        public MultipleChoice { options = List.copyOf(options); }
    }
    record NumericAnswer(String prompt, String placeholder) implements BlockConfiguration { }
    record TextAnswer(String prompt, boolean multiline) implements BlockConfiguration { }
    record Simulation(String pluginId, String message) implements BlockConfiguration { }
    record WorkspaceLauncher(String label, String documentType) implements BlockConfiguration { }
}