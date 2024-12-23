package controller;

import java.util.ArrayList;
import java.util.Optional;

import database.UserDAO;
import models.User;

public class UserController {
	private UserDAO userDAO;

	public UserController() {
		this.userDAO = new UserDAO();
	}

	public void register(String id, String username, String email, String password, String role) {
		User newUser = new User(id, username, email, password, role);
		userDAO.insertUser(newUser);
	}

    public Optional<User> login(String email, String password){
        return userDAO.authenticateUser(email, password);
    }
    
	public void changeProfile(String id, String username, String email, String password, String role) {
		User updatedUser = new User(id, username, email, password, role);
		userDAO.updateUser(updatedUser);
	}

	public User getUserById(String userId) {
		return userDAO.getUserByUserId(userId);
	}
	
	public User getUserByEmail(String email) {
		return userDAO.getUserByEmail(email);
	}
	
	public User getUserByUsername(String name) {
		return userDAO.getUserByUsername(name);
	}
	
	public String checkLoginInput(String email, String password)
	{
		if (email.isEmpty()) {
			return "Email cannot be empty!";
		}
		if(password.isEmpty())
		{
			return "Password cannot be empty!";
		}
		return null;
	}
	
	public String checkRegisterInput(String username, String email, String password, String role){
		if (username.isEmpty()) {
            return "Username cannot be empty.";
        }
        if (!userDAO.isUsernameUnique(username)) {
            return "Username must be unique.";
        }
        if (email.isEmpty()) {
            return "Email cannot be empty.";
        }
        if (!userDAO.isEmailUnique(email)) {
            return "Email must be unique.";
        }
        if (password.isEmpty()) {
            return "Password cannot be empty.";
        }
        if (password.length() < 5) {
            return "Password must be at least 5 characters long.";
        }
        if (role == null) {
            return "Role must be selected.";
        }
        return null;
	}

	public String checkChangeProfileInput(User currentUser, String newName, String newEmail, String oldPassword, String newPassword) {
        if (newName.isEmpty() && newEmail.isEmpty() && newPassword.isEmpty()) {
            return "At least one field must be updated.";
        }

        if (!newEmail.isEmpty() && newEmail.equals(currentUser.getUser_email())) {
            return "The new email must be different from the current email.";
        }

        if (!newName.isEmpty() && newName.equals(currentUser.getUser_name())) {
            return "The new name must be different from the current name.";
        }

        if (!newEmail.isEmpty() && !userDAO.isEmailUnique(newEmail)) {
            return "The email is already taken by another user.";
        }

        if (!newName.isEmpty() && !userDAO.isUsernameUnique(newName)) {
            return "The name is already taken by another user.";
        }

        if (!oldPassword.equals(currentUser.getUser_password())) {
            return "Old password is incorrect.";
        }

        if (newPassword.length() < 5) {
            return "The new password must be at least 5 characters long.";
        }

        return null;
    }
	
	public boolean updateUserProfile(String userId, String newName, String newEmail, String oldPassword, String newPassword) {
		User user = userDAO.getUserByUserId(userId);

		if (user == null) {
			return false;
		}

		if (!user.getUser_password().equals(oldPassword)) {
			return false;
		}

		user.setUser_name(newName);
		user.setUser_email(newEmail);

		if (newPassword != null && !newPassword.isEmpty()) {
			user.setUser_password(newPassword);
		}

		return userDAO.updateUser(user);
	}

}
