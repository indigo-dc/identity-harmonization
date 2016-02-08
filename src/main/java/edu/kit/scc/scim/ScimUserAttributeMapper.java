/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.scim;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.mail.internet.InternetAddress;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import edu.kit.scc.dto.IndigoUser;
import edu.kit.scc.scim.ScimUser.Address;
import edu.kit.scc.scim.ScimUser.Email;
import edu.kit.scc.scim.ScimUser.Meta;
import edu.kit.scc.scim.ScimUser.Name;
import edu.kit.scc.scim.ScimUser.PhoneNumber;
import edu.kit.scc.scim.ScimUser.Photo;

public class ScimUserAttributeMapper {

	private static final Logger log = LoggerFactory.getLogger(ScimUserAttributeMapper.class);

	public IndigoUser mapToIndigoUser(ScimUser scimUser) {
		IndigoUser user = new IndigoUser();

		user.setIndigoId(scimUser.getExternalId());
		user.setUid(scimUser.getUserName());

		Name name = scimUser.getName();
		user.setCommonName(name.getGivenName());
		user.setSurName(name.getFamilyName());

		if (scimUser.getMeta() != null) {
			if (scimUser.getMeta().get("homeDirectory") != null)
				user.setHomeDirectory(scimUser.getMeta().get("homeDirectory"));
			if (scimUser.getMeta().get("gecos") != null)
				user.setGecos(scimUser.getMeta().get("gecos"));
			if (scimUser.getMeta().get("loginShell") != null)
				user.setLoginShell(scimUser.getMeta().get("loginShell"));
			if (scimUser.getMeta().get("description") != null)
				user.setDescription(scimUser.getMeta().get("description"));
			if (scimUser.getMeta().get("gidNumber") != null) {
				user.setGidNumber(Integer.valueOf(scimUser.getMeta().get("gidNumber")));
			}
			if (scimUser.getMeta().get("uidNumber") != null) {
				user.setUidNumber(Integer.valueOf(scimUser.getMeta().get("gidNumber")));
			}
		}

		if (scimUser.getPassword() != null)
			user.setUserPassword(scimUser.getPassword().getBytes());

		return user;
	}

	public ScimUser mapFromIndigoUser(IndigoUser user) {
		ScimUser scimUser = new ScimUser();
		scimUser.setSchemas(Arrays.asList(scimUser.USER_SCHEMA_2_0));

		scimUser.setUserName(user.getUid());
		scimUser.setExternalId(user.getIndigoId());
		scimUser.setId(String.valueOf(user.getUidNumber()));

		Name name = new Name();
		name.setFamilyName(user.getSurName());
		name.setGivenName(user.getCommonName());
		scimUser.setName(name);

		Meta meta = new Meta();
		meta.put("homeDirectory", user.getHomeDirectory());
		meta.put("gecos", user.getGecos());
		meta.put("loginShell", user.getLoginShell());
		meta.put("description", user.getDescription());
		meta.put("gidNumber", String.valueOf(user.getGidNumber()));
		meta.put("uidNumber", String.valueOf(user.getUidNumber()));

		if (user.getUserPassword() != null)
			scimUser.setPassword(new String(user.getUserPassword()));

		return scimUser;
	}

	@Deprecated
	public ScimUser mapFromRegAppQuery(String query) {
		ScimUser scimUser = new ScimUser();
		scimUser.setSchemas(Arrays.asList(scimUser.USER_SCHEMA_2_0));

		try {
			JSONObject jsonQuery = new JSONObject(query);

			String eppn = jsonQuery.getString("eppn");
			if (eppn != null) {
				String userName = eppn.split("@")[0];
				scimUser.setUserName(userName);
			}
			String mail = jsonQuery.getString("mail");
			if (mail != null) {
				if (scimUser.getEmails() == null)
					scimUser.setEmails(new ArrayList<Email>());

				Email email = new Email();
				email.setValue(mail);

				scimUser.getEmails().add(email);
			}
			String lastUpdate = jsonQuery.getString("last_update");
			if (lastUpdate != null) {
				if (scimUser.getMeta() == null)
					scimUser.setMeta(new Meta());

				scimUser.getMeta().put("lastModified", lastUpdate);
			}
		} catch (JSONException e) {

		} catch (ArrayIndexOutOfBoundsException e) {

		}
		return scimUser;
	}

	@Deprecated
	public ScimUser mapFromUserInfo(UserInfo userInfo) {
		ScimUser scimUser = new ScimUser();
		scimUser.setSchemas(Arrays.asList(scimUser.USER_SCHEMA_2_0));

		com.nimbusds.openid.connect.sdk.claims.Address address = userInfo.getAddress();
		if (address != null) {
			scimUser.setAddresses(new ArrayList<Address>());
			Address scimAddress = new Address();

			scimAddress.setCountry(address.getCountry());
			scimAddress.setLocality(address.getLocality());
			scimAddress.setPostalCode(address.getPostalCode());
			scimAddress.setRegion(address.getRegion());
			scimAddress.setFormatted(address.getFormatted());
			scimAddress.setStreetAddress(address.getStreetAddress());

			scimUser.getAddresses().add(scimAddress);
		}
		InternetAddress email = userInfo.getEmail();
		if (email != null) {
			scimUser.setEmails(new ArrayList<Email>());
			Email scimEmail = new Email();

			scimEmail.setValue(email.getAddress());

			scimUser.getEmails().add(scimEmail);
		}
		String phoneNumber = userInfo.getPhoneNumber();
		if (phoneNumber != null) {
			scimUser.setPhoneNumbers(new ArrayList<PhoneNumber>());
			PhoneNumber scimPhoneNumber = new PhoneNumber();

			scimPhoneNumber.setValue(phoneNumber);

			scimUser.getPhoneNumbers().add(scimPhoneNumber);
		}
		String familyName = userInfo.getFamilyName();
		if (familyName != null) {
			if (scimUser.getName() == null)
				scimUser.setName(new Name());
			scimUser.getName().setFamilyName(familyName);
		}
		String givenName = userInfo.getGivenName();
		if (givenName != null) {
			if (scimUser.getName() == null)
				scimUser.setName(new Name());
			scimUser.getName().setGivenName(givenName);
		}
		String locale = userInfo.getLocale();
		if (locale != null)
			scimUser.setLocale(locale);
		String middleName = userInfo.getMiddleName();
		if (middleName != null) {
			if (scimUser.getName() == null)
				scimUser.setName(new Name());
			scimUser.getName().setMiddleName(middleName);
		}
		String userName = userInfo.getName();
		if (userName != null)
			scimUser.setUserName(userName);
		String nickName = userInfo.getNickname();
		if (nickName != null)
			scimUser.setNickName(nickName);
		URI picture = userInfo.getPicture();
		if (picture != null) {
			scimUser.setPhotos(new ArrayList<Photo>());

			Photo photo = new Photo();
			photo.setValue(picture.toString());

			scimUser.getPhotos().add(photo);
		}
		String displayName = userInfo.getPreferredUsername();
		if (displayName != null)
			scimUser.setDisplayName(displayName);
		URI profile = userInfo.getProfile();
		if (profile != null)
			scimUser.setProfileUrl(profile.toString());
		Subject subject = userInfo.getSubject();
		if (subject != null)
			scimUser.setExternalId(subject.getValue());
		Date updateTime = userInfo.getUpdatedTime();
		if (updateTime != null) {
			if (scimUser.getMeta() == null)
				scimUser.setMeta(new Meta());

			scimUser.getMeta().put("lastModified", updateTime.toString());
		}
		URI website = userInfo.getWebsite();
		if (website != null) {
			if (scimUser.getMeta() == null)
				scimUser.setMeta(new Meta());

			scimUser.getMeta().put("website", website.toString());
		}
		String timezone = userInfo.getZoneinfo();
		if (timezone != null)
			scimUser.setTimezone(timezone);

		return scimUser;
	}

	public ScimUser mapFromScim1User(ScimUser1_0 scim1User) {
		ScimUser scimUser = new ScimUser();
		scimUser.setSchemas(Arrays.asList(scimUser.USER_SCHEMA_2_0));

		List<String> emails = scim1User.getEmails();
		if (emails != null) {
			if (scimUser.getEmails() == null)
				scimUser.setEmails(new ArrayList<Email>());

			for (String email : emails) {
				Email newEmail = new Email();
				newEmail.setValue(email);
				scimUser.getEmails().add(newEmail);
			}
		}

		List<edu.kit.scc.scim.ScimUser1_0.Group> groups = scim1User.getGroups();
		if (groups != null) {
			if (scimUser.getGroups() == null)
				scimUser.setGroups(new ArrayList<ScimGroup>());

			for (edu.kit.scc.scim.ScimUser1_0.Group group : groups) {
				ScimGroup newGroup = new ScimGroup();
				newGroup.setDisplay(group.getDisplay());
				newGroup.setValue(group.getValue());
				scimUser.getGroups().add(newGroup);
			}
		}

		String id = scim1User.getId();
		if (id != null)
			scimUser.setId(id);

		edu.kit.scc.scim.ScimUser1_0.Meta meta = scim1User.getMeta();
		if (meta != null) {
			if (scimUser.getMeta() == null)
				scimUser.setMeta(new Meta());

			for (Entry<String, String> entry : meta.entrySet()) {
				scimUser.getMeta().put(entry.getKey(), entry.getValue());
			}
		}

		edu.kit.scc.scim.ScimUser1_0.Name name = scim1User.getName();
		if (name != null) {
			if (scimUser.getName() == null)
				scimUser.setName(new Name());

			scimUser.getName().setFamilyName(name.getFamilyName());
			scimUser.getName().setGivenName(name.getGivenName());
		}

		String userName = scim1User.getUserName();
		if (userName != null)
			scimUser.setUserName(userName);

		return scimUser;
	}

	// TODO
	// for now only user name and groups are checked
	// user info from IdP have precedence, groups are added from SCIM provider
	@Deprecated
	public ScimUser merge(ScimUser scimUserFromScimProvider, ScimUser scimUserFromIdP) {
		if (scimUserFromIdP == null) {
			log.warn("SCIM user from IdP null");
			return scimUserFromScimProvider;
		}
		if (scimUserFromScimProvider == null) {
			log.warn("SCIM user from SCIM provider null");
			return scimUserFromIdP;
		}

		log.debug("merge {} with {}", scimUserFromScimProvider.toString(), scimUserFromIdP.toString());

		ScimUser aggregate = scimUserFromIdP;

		String userNameFromScimProvider = scimUserFromScimProvider.getUserName();
		String userNameFromIdP = scimUserFromIdP.getUserName();

		if (userNameFromScimProvider != null && userNameFromIdP != null) {
			if (!userNameFromScimProvider.equals(userNameFromIdP)) {
				log.warn("CONFLICT SCIM user name {} with IdP user name {}", userNameFromScimProvider, userNameFromIdP);
				log.debug("Merged user info {}", aggregate.toString());
				return aggregate;
			}
		}

		List<ScimGroup> groups = scimUserFromScimProvider.getGroups();
		if (groups != null && !groups.isEmpty()) {
			if (aggregate.getGroups() == null) {
				aggregate.setGroups(groups);
			} else {
				aggregate.getGroups().addAll(groups);
			}
		}
		log.debug("Merged user info {}", aggregate.toString());
		return aggregate;
	}
}
