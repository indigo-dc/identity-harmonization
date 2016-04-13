/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.dao;

import edu.kit.scc.dto.PosixGroup;

import java.util.List;

public interface PosixGroupDao {
  public List<PosixGroup> getAllGroups();

  public List<PosixGroup> getGroupDetails(String commonName);

  public void insertGroup(PosixGroup group);

  public void updateGroup(PosixGroup group);

  public void deleteGroup(PosixGroup group);
}
