package az.aztu.egrant.expert.internal;

import az.aztu.egrant.project.api.ProjectSubmitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@link ProjectSubmitted}: a submitted project becomes eligible for expert assignment.
 * (Assignment remains an explicit admin action; this records eligibility in the review subsystem.)
 */
@Component
public class ProjectSubmittedListener {

    private static final Logger log = LoggerFactory.getLogger(ProjectSubmittedListener.class);

    @EventListener
    public void onProjectSubmitted(ProjectSubmitted event) {
        log.info("Project {} (code {}) submitted — now eligible for expert assignment.",
                event.projectId(), event.projectCode());
    }
}
