package ai.multica.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class IssueBuckets {
    private IssueBuckets() {}

    interface Fetcher {
        Models.Page<Models.Issue> fetch(String status, int limit, int offset) throws Exception;
    }

    static List<Models.Issue> loadAll(Fetcher fetcher, int limit) throws Exception {
        List<Models.Issue> all = new ArrayList<>();
        for (String status : Models.STATUS_VALUES) {
            all.addAll(loadStatus(fetcher, status, limit));
        }
        return all;
    }

    static List<Models.Issue> loadAllConcurrent(Fetcher fetcher, int limit) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(4, Models.STATUS_VALUES.length));
        try {
            List<Future<List<Models.Issue>>> futures = new ArrayList<>();
            for (String status : Models.STATUS_VALUES) {
                Callable<List<Models.Issue>> task = () -> loadStatus(fetcher, status, limit);
                futures.add(pool.submit(task));
            }
            List<Models.Issue> all = new ArrayList<>();
            for (Future<List<Models.Issue>> future : futures) all.addAll(future.get());
            return all;
        } finally {
            pool.shutdownNow();
        }
    }

    private static List<Models.Issue> loadStatus(Fetcher fetcher, String status, int limit) throws Exception {
        List<Models.Issue> issues = new ArrayList<>();
        int offset = 0;
        while (true) {
            Models.Page<Models.Issue> page = fetcher.fetch(status, limit, offset);
            issues.addAll(page.items);
            if (!page.hasMore || page.items.isEmpty()) break;
            offset += page.items.size();
        }
        return issues;
    }
}
