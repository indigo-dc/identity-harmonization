/*   Copyright 2016 Karlsruhe Institute of Technology (KIT)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0
 */
package edu.kit.scc.scim;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.internet.InternetAddress;

import com.nimbusds.oauth2.sdk.id.Subject;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import edu.kit.scc.scim.ScimUser.Address;
import edu.kit.scc.scim.ScimUser.Email;
import edu.kit.scc.scim.ScimUser.Meta;
import edu.kit.scc.scim.ScimUser.Name;
import edu.kit.scc.scim.ScimUser.PhoneNumber;
import edu.kit.scc.scim.ScimUser.Photo;

public class ScimUserAttributeMapper {

	public ScimUser mapFromUserInfo(UserInfo userInfo) {
		ScimUser scimUser = new ScimUser();

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
}
