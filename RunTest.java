import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class RunTest {
    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(co.com.pragma.usecase.auth.DiagnosticTest.class))
            .build();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        
        TestExecutionSummary summary = listener.getSummary();
        System.out.println("Tests run: " + summary.getTestsStartedCount() + 
                         ", Failures: " + summary.getTestsFailedCount());
        
        if (summary.getFailures().size() > 0) {
            System.out.println("\nFailures:");
            summary.getFailures().forEach(failure -> {
                System.out.println(" - " + failure.getTestIdentifier().getDisplayName());
                failure.getException().printStackTrace();
            });
        }
        
        System.exit(summary.getTestsFailedCount() > 0 ? 1 : 0);
    }
}
