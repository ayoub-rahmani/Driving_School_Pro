package org.example.Service;

import org.example.Entities.User;
import org.example.Rep.UserRep;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class UserService {
    private UserRep userRep;

    public UserService() throws SQLException {
        userRep = new UserRep();
    }
    public boolean addUser(User user) throws SQLException {
        return userRep.add(user);
    }


    public boolean updateUser(User user) throws SQLException {
        return userRep.update(user);
    }

    public boolean deleteUser(int id) throws SQLException {
        return userRep.delete(id);
    }

    public User findById(int id) throws SQLException {
        return userRep.findById(id);
    }

    public User findByUsernamePassword(String username,String password) throws SQLException {
        return userRep.findByUsernamePassword(username,password);
    }
    public User findByUsername(String username) throws SQLException {
        return userRep.findByUsername(username);
    }

    public User findByEmail(String email) throws SQLException {
        return userRep.findByEmail(email);
    }

    public User findByPhoneNumber(String phoneNumber) throws SQLException {
        return userRep.findByPhoneNumber(phoneNumber);
    }

    public List<User> findAll() throws SQLException {
        return userRep.findAll();
    }

    public User authenticate(String username, String password) throws SQLException {
        User user = userRep.findByUsernamePassword(username,password);

        if (user!=null){// Update last login time
            user.setLastLogin(new Timestamp(System.currentTimeMillis()));
            userRep.updateLastLogin(user.getId(), user.getLastLogin());
            return user;}
        return null;

    }

    public boolean updatePassword(int userId, String newPassword) throws SQLException {
        return userRep.updatePassword(userId, newPassword);
    }

    public boolean updateUsername(int userId, String newUsername) throws SQLException {
        return userRep.updateUsername(userId, newUsername);
    }

    public boolean updateEmail(int userId, String newEmail) throws SQLException {
        return userRep.updateEmail(userId, newEmail);
    }

    public boolean updatePhoneNumber(int userId, String newPhoneNumber) throws SQLException {
        return userRep.updatePhoneNumber(userId, newPhoneNumber);
    }

    public boolean updateRole(int userId, String newRole) throws SQLException {
        return userRep.updateRole(userId, newRole);
    }

    public boolean updateFullName(int userId, String newFullName) throws SQLException {
        return userRep.updateFullName(userId, newFullName);
    }

    public boolean updateActive(int userId, boolean active) throws SQLException {
        return userRep.updateActive(userId, active);
    }

    public boolean isUsernameExists(String username , String password ) throws SQLException {
        return userRep.findByUsernamePassword(username,password) != null;
    }

    public boolean isEmailExists(String email) throws SQLException {
        return userRep.findByEmail(email) != null;
    }

    public boolean isPhoneNumberExists(String phoneNumber) throws SQLException {
        return userRep.findByPhoneNumber(phoneNumber) != null;
    }
}