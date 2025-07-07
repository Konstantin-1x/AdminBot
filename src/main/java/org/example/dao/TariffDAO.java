package org.example.dao;

import org.example.table.Tariff;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.List;

public class TariffDAO {

    private final SessionFactory sessionFactory;

    public TariffDAO() {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Не удалось создать SessionFactory: " + ex);
        }
    }

    public void save(Tariff tariff) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(tariff);
            tx.commit();
        }
    }

    public void update(Tariff tariff) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(tariff);
            tx.commit();
        }
    }
    public int getNextId() {
        try (Session session = sessionFactory.openSession()) {
            Integer maxId = session.createQuery("SELECT MAX(t.id) FROM Tariff t", Integer.class)
                    .uniqueResult();
            return (maxId != null ? maxId : 0) + 1;
        }
    }

    public Tariff findById(int id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Tariff.class, id);
        }
    }

    public List<Tariff> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Tariff", Tariff.class).list();
        }
    }
    public List<Tariff> findAllVisible() {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("from Tariff where visible = true", Tariff.class)
                    .list();
        }
    }

    public void deleteById(int id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Tariff tariff = session.get(Tariff.class, id);
            if (tariff != null) {
                session.remove(tariff);
            }
            tx.commit();
        }
    }

    public void close() {
        sessionFactory.close();
    }

    public void updateNameById(int id, String newName) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Query<?> query = session.createQuery("update Tariff set name = :name where id = :id");
            query.setParameter("name", newName);
            query.setParameter("id", id);
            query.executeUpdate();
            tx.commit();
        }
    }

    public void updateDescriptionById(int id, String newDescription) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Query<?> query = session.createQuery("update Tariff set description = :description where id = :id");
            query.setParameter("description", newDescription);
            query.setParameter("id", id);
            query.executeUpdate();
            tx.commit();
        }
    }

    public void updatePriceById(int id, BigDecimal newPrice) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Query<?> query = session.createQuery("update Tariff set price = :price where id = :id");
            query.setParameter("price", newPrice);
            query.setParameter("id", id);
            query.executeUpdate();
            tx.commit();
        }
    }
    public void updateDiscountById(int id, BigDecimal discount) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Query<?> query = session.createQuery("update Tariff set discount = :discount where id = :id");
            query.setParameter("discount", discount);
            query.setParameter("id", id);
            query.executeUpdate();
            tx.commit();
        }
    }

    public void updateTermById(int id, int newTerm) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Query<?> query = session.createQuery("update Tariff set term = :term where id = :id");
            query.setParameter("term", newTerm);
            query.setParameter("id", id);
            query.executeUpdate();
            tx.commit();
        }
    }
    public void updateVisibleById(int id, boolean visible) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            Query<?> query = session.createQuery("update Tariff set visible = :visible where id = :id");
            query.setParameter("visible", visible);
            query.setParameter("id", id);
            query.executeUpdate();
            tx.commit();
        }
    }


}
