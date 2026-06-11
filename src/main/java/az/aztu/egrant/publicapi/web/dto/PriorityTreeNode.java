package az.aztu.egrant.publicapi.web.dto;

import java.util.List;

/** A priority with its approved projects (the legacy "leads-tree"). */
public record PriorityTreeNode(
        Integer code,
        String name,
        List<PublicProjectSummary> projects) {
}
