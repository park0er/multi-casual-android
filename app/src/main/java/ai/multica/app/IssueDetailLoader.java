package ai.multica.app;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class IssueDetailLoader {
    interface Fetcher {
        Models.Issue issue(String issueId, String workspaceId) throws Exception;

        List<Models.Comment> comments(String issueId, String workspaceId) throws Exception;

        List<Models.AgentTask> runs(String issueId, String workspaceId) throws Exception;

        List<Models.Project> projects() throws Exception;

        List<Models.Member> members() throws Exception;

        List<Models.Agent> agents() throws Exception;
    }

    static Data load(String issueId, String workspaceId, Fetcher fetcher, int poolSize) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, poolSize));
        try {
            Future<Models.Issue> issueFuture = pool.submit(() -> fetcher.issue(issueId, workspaceId));
            Future<List<Models.Comment>> commentsFuture = null;
            Future<List<Models.AgentTask>> runsFuture = null;
            if (workspaceId != null && !workspaceId.isEmpty()) {
                commentsFuture = pool.submit(() -> fetcher.comments(issueId, workspaceId));
                runsFuture = pool.submit(() -> fetcher.runs(issueId, workspaceId));
            }
            Future<List<Models.Project>> projectsFuture = pool.submit(fetcher::projects);
            Future<List<Models.Member>> membersFuture = pool.submit(fetcher::members);
            Future<List<Models.Agent>> agentsFuture = pool.submit(fetcher::agents);

            Data data = new Data();
            data.issue = get(issueFuture);
            String resolvedWorkspaceId = data.issue.workspaceId;
            if (resolvedWorkspaceId == null || resolvedWorkspaceId.isEmpty()) resolvedWorkspaceId = workspaceId;
            final String finalWorkspaceId = resolvedWorkspaceId;
            if (commentsFuture == null) commentsFuture = pool.submit(() -> fetcher.comments(issueId, finalWorkspaceId));
            if (runsFuture == null) runsFuture = pool.submit(() -> fetcher.runs(issueId, finalWorkspaceId));
            data.comments = get(commentsFuture);
            data.runs = get(runsFuture);
            data.projects = get(projectsFuture);
            data.members = get(membersFuture);
            data.agents = get(agentsFuture);
            return data;
        } finally {
            pool.shutdownNow();
        }
    }

    private static <T> T get(Future<T> future) throws Exception {
        try {
            return future.get();
        } catch (ExecutionException error) {
            Throwable cause = error.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw new RuntimeException(cause);
        }
    }

    static final class Data {
        Models.Issue issue;
        List<Models.Comment> comments;
        List<Models.AgentTask> runs;
        List<Models.Project> projects;
        List<Models.Member> members;
        List<Models.Agent> agents;
    }
}
