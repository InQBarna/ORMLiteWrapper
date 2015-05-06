package com.inqbarna.inqorm;

import com.j256.ormlite.misc.TransactionManager;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Created by David García <david.garcia@inqbarna.com> on 6/5/15.
 */
public class Transaction {
    private DataTool dataTool;
    private TransactionManager tm;
    Transaction(DataTool dataTool) {
        this.dataTool = dataTool;
        tm = new TransactionManager(dataTool.getConnectionSource());
    }


    public interface Job<T> {
        T execute(DataTool dataTool);
    }

    public <T> T runJob(final Job<T> job) {
        try {
            return tm.callInTransaction(
                    new Callable<T>() {
                        @Override
                        public T call() throws Exception {
                            return job.execute(dataTool);
                        }
                    }
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
