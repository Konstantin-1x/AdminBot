package org.example.dao;

import org.hibernate.query.Query;
import org.example.table.People;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;

public class PeopleDAO {

    private final SessionFactory sessionFactory;

    public PeopleDAO() {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
            System.out.println("✅ SessionFactory создан");
        } catch (Throwable ex) {
            System.err.println("🔥 Не удалось создать SessionFactory: " + ex.getMessage());
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }


    // 💾 Сохранение
    public void save(People people) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(people);
            tx.commit();
        }
    }

    // 🔄 Обновление
    public void update(People people) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(people);
            tx.commit();
        }
    }

    // 🔍 Поиск по TgID
    public People findById(long tgId) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(People.class, tgId);
        }
    }

    // 📋 Получение всех записей
    public List<People> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from People", People.class).list();
        }
    }
    public People findByUsername(String username) {
        try (Session session = sessionFactory.openSession()) {
            return session
                    .createQuery("FROM People WHERE username = :username", People.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }


    public void close() {
        sessionFactory.close();
    }

    public void updateAdminByTgId(long tgId, boolean admin) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Query<?> query = session.createQuery(
                    "update People set admin = :admin where tgId = :tgId"
            );
            query.setParameter("admin", admin);
            query.setParameter("tgId", tgId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
        }
    }

    public void updateUserByTgId(long tgId, boolean user_flag) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Query<?> query = session.createQuery(
                    "update People set user_flag = :user_flag where tgId = :tgId"
            );
            query.setParameter("user_flag", user_flag);
            query.setParameter("tgId", tgId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
        }
    }

    public void updateMarketingByTgId(long tgId, boolean marketing) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Query<?> query = session.createQuery(
                    "update People set marketing = :marketing where tgId = :tgId"
            );
            query.setParameter("marketing", marketing);
            query.setParameter("tgId", tgId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
        }
    }

    public void updateActiveByTgId(long tgId, boolean active) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Query<?> query = session.createQuery(
                    "update People set active = :active where tgId = :tgId"
            );
            query.setParameter("active", active);
            query.setParameter("tgId", tgId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
        }
    }

    public void updateSubscriptionTimeByTgId(long tgId, String newTime) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            Query<?> query = session.createQuery(
                    "update People set subscriptionTime = :time where tgId = :tgId"
            );
            query.setParameter("time", newTime);
            query.setParameter("tgId", tgId);
            query.executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
        }
    }
}
