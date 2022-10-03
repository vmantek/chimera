package com.vmantek.chimera.tm;

import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.Context;
import org.jpos.transaction.TxnConstants;
import org.jpos.transaction.TxnSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.io.Serializable;

@SuppressWarnings({"SpringJavaAutowiredMembersInspection", "SpringAutowiredFieldsWarningInspection", "unused"})
public class Close extends TxnSupport implements AbortParticipant
{
    @Autowired
    PlatformTransactionManager transactionManager;

    public void setTransactionManager(PlatformTransactionManager tm)
    {
        this.transactionManager = tm;
    }

    public int prepare(long id, Serializable o)
    {
        return PREPARED | READONLY;
    }

    public int prepareForAbort(long id, Serializable o)
    {
        return PREPARED | READONLY;
    }

    public void commit(long id, Serializable o)
    {
        closeDB(o);
    }

    public void abort(long id, Serializable o)
    {
        closeDB(o);
    }

    private void closeDB(Serializable o)
    {
        Context ctx = (Context) o;
        TransactionStatus tx = ctx.get(TX);
        try
        {
            try
            {
                if(tx!=null)
                {
                    transactionManager.commit(tx);
                    ctx.remove(TxnConstants.TX);
                }
            }
            catch (RuntimeException t)
            {
                error(t);
                try
                {
                    if(!tx.isCompleted()) transactionManager.rollback(tx);
                }
                catch (RuntimeException rte)
                {
                    error("Rollback error", rte);
                }
            }
        }
        catch (RuntimeException ex)
        {
            error(ex);
        }
        checkPoint(ctx);
    }
}
