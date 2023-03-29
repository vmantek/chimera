package com.vmantek.chimera.db;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jpos.ee.DB;
import org.jpos.util.Log;

import javax.persistence.EntityManager;

public class NewDB extends DB
{
    private EntityManager entityManager;

    public NewDB()
    {
    }

    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    public NewDB(String configModifier)
    {
    }

    public NewDB(Log log)
    {
    }

    public NewDB(Log log, String configModifier)
    {
    }

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

    @Override
    public void save(Object obj) throws HibernateException
    {
        entityManager.persist(obj);
    }

    @Override
    public void saveOrUpdate(Object obj) throws HibernateException
    {
        entityManager.merge(obj);
    }

    @Override
    public void delete(Object obj)
    {
        entityManager.remove(obj);
    }
}
