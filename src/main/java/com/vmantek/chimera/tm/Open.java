package com.vmantek.chimera.tm;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.ee.DB;
import org.jpos.transaction.Context;
import org.jpos.transaction.TxnSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@Component
public class Open extends TxnSupport
{
    @PersistenceContext
    EntityManager entityManager;

    int timeout = 0;

    final PlatformTransactionManager tm;

    public Open(PlatformTransactionManager tm)
    {
        this.tm = tm;
    }

    public int prepare(long id, Serializable o)
    {
        int rc = ABORTED;
        Context ctx = (Context) o;
        try
        {
            getDatabase(ctx);
            beginTransaction(ctx);
            checkPoint(ctx);
            rc = PREPARED;
        }
        catch (Throwable t)
        {
            error(t);
        }
        return rc | NO_JOIN | READONLY;
    }

    public void setConfiguration(Configuration cfg)
        throws ConfigurationException
    {
        super.setConfiguration(cfg);
        this.timeout = cfg.getInt("timeout", 0);
    }

    private void beginTransaction(Context ctx)
    {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        if (timeout > 0)
        {
            def.setTimeout(timeout);
        }
        TransactionStatus tx = tm.getTransaction(def);
        ctx.put(TX, tx);
    }

    public void commit(long id, Serializable o)
    {
    }

    public void abort(long id, Serializable o)
    {
    }

    @SuppressWarnings("UnusedReturnValue")
    private DB getDatabase(Context ctx)
    {
        DB db = ctx.get(DB);
        if (db == null)
        {
            ctx.put(DB, db = new DB() {
                @Override
                public SessionFactory getSessionFactory()
                {
                    return null;
                }

                @Override
                public Session session()
                {
                    return entityManager.unwrap(Session.class);
                }
            });
        }
        return db;
    }
}
