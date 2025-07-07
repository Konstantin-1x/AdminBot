package org.example;

import org.example.dao.PeopleDAO;
import org.example.table.People;

public class Admin {
    public boolean isAdmin(long chatId){
        PeopleDAO peopleDAO = new PeopleDAO();
        People people = peopleDAO.findById(chatId);

        return people.isAdmin();
    }
}
