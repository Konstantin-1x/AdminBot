package org.example.dao;

import org.example.table.GroupTG;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.List;

public class GroupTGDAO {

    private final SessionFactory sessionFactory;

    public GroupTGDAO() {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Failed to create SessionFactory: " + ex);
        }
    }

    public void save(GroupTG groupTG) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(groupTG);
            tx.commit();
        }
    }

    public void update(GroupTG groupTG) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(groupTG);
            tx.commit();
        }
    }

    public GroupTG findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(GroupTG.class, id);
        }
    }

    public List<GroupTG> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from GroupTG", GroupTG.class).list();
        }
    }

    public void deleteById(long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            GroupTG group = session.get(GroupTG.class, id);
            if (group != null) {
                session.remove(group);
            }
            tx.commit();
        }
    }

    public void incrementSizeByGroupId(long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Query<?> query = session.createQuery("update GroupTG set size = size + 1 where groupID = :id");
            query.setParameter("id", id);
            query.executeUpdate();
            tx.commit();
        }
    }
    public GroupTG findFirst() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM GroupTG", GroupTG.class)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    public void close() {
        sessionFactory.close();
    }
}

