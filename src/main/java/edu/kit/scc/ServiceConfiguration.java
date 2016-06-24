/*
 * Copyright 2016 Karlsruhe Institute of Technology (KIT)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package edu.kit.scc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
public class ServiceConfiguration {

  @Value("${ldap.port}")
  private int ldapPort;

  @Value("${ldap.url}")
  private String url;

  @Value("${ldap.searchBase}")
  private String searchBase;

  @Value("${ldap.bindDn}")
  private String bindDn;

  @Value("${ldap.bindPassword}")
  private String password;

  @Value("${spring.redis.port}")
  private int port;

  @Bean
  JedisConnectionFactory jedisConnectionFactory() {
    JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
    jedisConnectionFactory.setPort(port);
    return jedisConnectionFactory;
  }

  @Bean
  StringRedisTemplate template(JedisConnectionFactory jedisConnectionFactory) {
    return new StringRedisTemplate(jedisConnectionFactory);
  }

  @Bean
  LdapContextSource contextSource() {
    LdapContextSource ldapContextSource = new LdapContextSource();
    ldapContextSource.setUrl(url);
    ldapContextSource.setBase(searchBase);
    ldapContextSource.setUserDn(bindDn);
    ldapContextSource.setPassword(password);
    return ldapContextSource;
  }

  @Bean
  LdapTemplate ldapTemplate(LdapContextSource contextSource) {
    return new LdapTemplate(contextSource);
  }
}
