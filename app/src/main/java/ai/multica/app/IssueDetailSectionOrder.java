package ai.multica.app;

import java.util.ArrayList;
import java.util.List;

final class IssueDetailSectionOrder {
    enum Section {
        HERO,
        COMPACT_METADATA,
        DESCRIPTION,
        LATEST_PROGRESS,
        SUB_ISSUES,
        SUBSCRIBERS,
        COMMENTS,
        AGENT_WORK_DETAILS,
        MORE_DETAILS,
        USAGE,
        ACTIVITY
    }

    static List<Section> defaultReadingPath(
            boolean hasAgentWork,
            boolean hasActivity
    ) {
        ArrayList<Section> sections = new ArrayList<>();
        sections.add(Section.HERO);
        sections.add(Section.COMPACT_METADATA);
        sections.add(Section.DESCRIPTION);
        sections.add(Section.LATEST_PROGRESS);
        sections.add(Section.SUB_ISSUES);
        sections.add(Section.SUBSCRIBERS);
        sections.add(Section.COMMENTS);
        if (hasAgentWork) sections.add(Section.AGENT_WORK_DETAILS);
        sections.add(Section.MORE_DETAILS);
        sections.add(Section.USAGE);
        if (hasActivity) sections.add(Section.ACTIVITY);
        return sections;
    }

    private IssueDetailSectionOrder() {
    }
}
