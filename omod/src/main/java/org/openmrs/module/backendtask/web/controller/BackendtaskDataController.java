/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.backendtask.web.controller;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/backendtask")
public class BackendtaskDataController extends BaseRestController {
	
	private final PatientService patientService = Context.getService(PatientService.class);
	
	private final List<Patient> patients = patientService.getAllPatients();
	
	@RequestMapping(value="/{term}", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public Patient getPatientByNameOrMobileNumber(@PathVariable("term") String term){
		Predicate<Patient> familyNamePredicate   = p -> p.getPerson().getFamilyName().equals(term);
		Predicate<Patient> mobileNumberPredicate = p -> Objects.equals(p.getAttribute("Telephone Number").toString(), term);
		Patient patient = getPatient(term, familyNamePredicate);
		if (patient == null) {
			patient = getPatient(term, mobileNumberPredicate);
		}
		return patient;
	}
	
	private Patient getPatient(String term, Predicate<Patient> predicate) {
		return patients.stream().filter(predicate).findAny().orElse(getPatientByMiddleNameOrGivenName(term));
	}
	
	private Patient getPatientByMiddleNameOrGivenName(String term){
		Predicate<Patient> middleNamePredicate = p -> p.getPerson().getMiddleName().equals(term);
		Predicate<Patient> givenNamePredicate  = p -> p.getPerson().getGivenName().equals(term);
		return patients.stream()
				.filter(middleNamePredicate.or(givenNamePredicate))
				.findAny().orElse(null);
	}
}
