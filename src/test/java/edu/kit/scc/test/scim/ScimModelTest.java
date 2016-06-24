/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc.test.scim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import edu.kit.scc.IdentityHarmonizationService;
import edu.kit.scc.scim.ScimUser;
import edu.kit.scc.scim.ScimUser.Address;
import edu.kit.scc.scim.ScimUser.Name;
import edu.kit.scc.scim.ScimUser10;
import edu.kit.scc.scim.ScimUser10.Meta;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IdentityHarmonizationService.class)
public class ScimModelTest {

  @Test
  public void createScimUser() {
    ScimUser scimUser = new ScimUser();

    assertNotNull(scimUser);

    Address address = new Address();

    assertNotNull(address);

    String str = "test";

    address.setCountry(str);
    address.setFormatted(str);
    address.setLocality(str);
    address.setPostalCode(str);
    address.setPrimary(true);
    address.setRegion(str);
    address.setStreetAddress(str);
    address.setType(str);

    assertEquals(str, address.getCountry());
    assertEquals(str, address.getFormatted());
    assertEquals(str, address.getLocality());
    assertEquals(str, address.getPostalCode());
    assertEquals(str, address.getRegion());
    assertEquals(str, address.getStreetAddress());
    assertEquals(str, address.getType());

    assertNotNull(address.toString());

    Name name = new Name();

    name.setFamilyName(str);
    name.setFormatted(str);
    name.setGivenName(str);
    name.setHonorificPrefix(str);
    name.setHonorificSufix(str);
    name.setMiddleName(str);

    assertEquals(str, name.getFamilyName());
    assertEquals(str, name.getFormatted());
    assertEquals(str, name.getGivenName());
    assertEquals(str, name.getHonorificPrefix());
    assertEquals(str, name.getHonorificSufix());
    assertEquals(str, name.getMiddleName());

    assertNotNull(name.toString());
  }

  @Test
  public void createScim1User() {
    ScimUser10 scimUser = new ScimUser10();

    String str = "test";

    scimUser.setId(str);
    scimUser.setEmails(Arrays.asList(str));
    scimUser.setUserName(str);

    Meta meta = new Meta();
    assertNotNull(meta);
    scimUser.setMeta(meta);

    edu.kit.scc.scim.ScimUser10.Name name = new edu.kit.scc.scim.ScimUser10.Name();
    name.setFamilyName(str);
    name.setGivenName(str);
    scimUser.setName(name);

    assertEquals(str, scimUser.getId());
    assertEquals(str, scimUser.getUserName());

    assertNotNull(scimUser);
    assertNotNull(scimUser.toString());
  }
}
