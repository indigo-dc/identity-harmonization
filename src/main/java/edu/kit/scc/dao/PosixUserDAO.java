/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.dao;

import java.util.List;

import edu.kit.scc.dto.PosixUser;

public interface PosixUserDAO {
	public List<PosixUser> getAllUsers();

	public List<PosixUser> getUserDetails(String uid);

	public void insertUser(PosixUser user);

	public void updateUser(PosixUser user);

	public void deleteUser(PosixUser user);
}