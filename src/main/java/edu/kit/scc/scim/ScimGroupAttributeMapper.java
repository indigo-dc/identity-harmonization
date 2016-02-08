/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.scim;

import edu.kit.scc.dto.PosixGroup;

public class ScimGroupAttributeMapper {

	public ScimGroup mapFromPosixGroup(PosixGroup group) {
		ScimGroup scimGroup = new ScimGroup();

		scimGroup.setValue(String.valueOf(group.getGidNumber()));
		scimGroup.setDisplay(group.getCommonName());

		if (group.getDescription() != null)
			scimGroup.set$ref(group.getDescription());

		return scimGroup;
	}

	public PosixGroup mapToPosixGroup(ScimGroup scimGroup) {
		PosixGroup group = new PosixGroup();

		group.setCommonName(scimGroup.getDisplay());
		group.setGidNumber(Integer.valueOf(scimGroup.getValue()));

		if (scimGroup.get$ref() != null)
			group.setDescription(scimGroup.get$ref());

		return group;
	}
}
