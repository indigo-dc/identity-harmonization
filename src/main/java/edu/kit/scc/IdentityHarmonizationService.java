/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.AbstractEnvironment;

@SpringBootApplication
public class IdentityHarmonizationService {

  /**
   * Spring Boot Application Runner.
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) {
    // set development environment
    //System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, "development");

    SpringApplication.run(IdentityHarmonizationService.class, args);

  }
}
