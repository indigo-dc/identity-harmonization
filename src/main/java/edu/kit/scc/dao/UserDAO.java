package edu.kit.scc.dao;

import java.util.List;

import edu.kit.scc.dto.UserDTO;

public interface UserDAO {
	public List<UserDTO> getAllUserNames();

	public List<UserDTO> getUserDetails(String commonName, String lastName);

	public void insertUser(UserDTO userDTO);

	public void updateUser(UserDTO userDTO);

	public void deleteUser(UserDTO userDTO);
}
