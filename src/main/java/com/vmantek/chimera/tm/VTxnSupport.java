package com.vmantek.chimera.tm;

import com.vmantek.chimera.db.HibernateUtil;
import org.jpos.transaction.Context;
import org.jpos.transaction.TxnSupport;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.io.Serializable;

public abstract class VTxnSupport extends TxnSupport
{
    @Autowired
    EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @Override
    public synchronized org.jpos.ee.DB getDB(Context ctx)
    {
        return HibernateUtil.getDB(entityManager, ctx);
    }

    @Override
    public void commit(long id, Serializable context)
    {

    }

    @Override
    public void abort(long id, Serializable context)
    {

    }
}
