/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.dao;

import java.util.List;

import edu.kit.scc.dto.PosixGroup;

public interface PosixGroupDAO {
	public List<PosixGroup> getAllGroups();

	public List<PosixGroup> getGroupDetails(String commonName);

	public void insertGroup(PosixGroup groupDTO);

	public void updateGroup(PosixGroup groupDTO);

	public void deleteGroup(PosixGroup groupDTO);

	public void addMember(PosixGroup groupDTO, String memberUid);
}
