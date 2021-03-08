package ssm.demo.core.processor.test.helper;

import java.util.Map;

public class FactsHelper {

	private static final Map<String, String> primaryApplicantFacts = Map.ofEntries(
			//			Screening Questions
			Map.entry("screening.applyingForSomeoneElseRelationshipNeedTrustee", "no"),
			//			Map.entry("screening.applyingForSomeoneElseRelationship", "RELATIONSHIP_TRUSTEE"),
			Map.entry("screening.receivingMoneyFromExistingProgram", "no"),
			Map.entry("screening.personWithDisability", "no"),
			Map.entry("screening.dateOfReleaseFromInstitution", "2020-01-01"),
			Map.entry("screening.livingOnFirstNationsReserveLand", "no"),
			Map.entry("screening.receivingMoneyForReason", "yes"),

			Map.entry("name.first", "Felicty"),
			Map.entry("name.last", "Fixit"),
			Map.entry("contact.email", "felcity.fixist@ontario.ca"),
			Map.entry("contact.phone", "1234567890"),
			Map.entry("birth.sex", "female"),
			Map.entry("birth.date", "2000-01-01"),
			Map.entry("contact.preferredLanguage", "french"),
			Map.entry("contact.remoteCommunityFarNorth", "no"),
			Map.entry("maritalStatus", "Single"),

			Map.entry("contact.address.living.deliveryType", "General delivery"),
			Map.entry("contact.address.living.generalDelivery", "General Delivery"),
			//				Map.entry("contact.address.living.ruralRoute", "Rural Route"),
			Map.entry("contact.address.living.station", "200"),
			//				Map.entry("contact.address.living.suburbanService", "Sub Service"),
			Map.entry("contact.address.living.apartmentNumber", "301"),
			Map.entry("contact.address.living.streetNumber", "50001"),
			Map.entry("contact.address.living.streetNumberSuffix", "1/4"),
			Map.entry("contact.address.living.streetName", "Yonge"),
			Map.entry("contact.address.living.streetType", "Avenue"),
			Map.entry("contact.address.living.direction", "East"),
			Map.entry("contact.address.living.city", "Toronto"),
			Map.entry("contact.address.living.province", "Ontario"),
			Map.entry("contact.address.living.postalCode", "L8M1J6"),
			Map.entry("contact.address.living.addressSiteComp", "100"),

			Map.entry("contact.address.mailing.deliveryType", "General delivery"),
			Map.entry("contact.address.mailing.apartmentNumber", "301"),
			Map.entry("contact.address.mailing.streetNumber", "50001"),
			Map.entry("contact.address.mailing.streetNumberSuffix", "1/4"),
			Map.entry("contact.address.mailing.streetName", "Yonge"),
			Map.entry("contact.address.mailing.streetType", "Avenue"),
			Map.entry("contact.address.mailing.direction", "East"),
			Map.entry("contact.address.mailing.poBox", "4"),
			Map.entry("contact.address.mailing.addressSiteComp", "100"),
			Map.entry("contact.address.mailing.station", "200"),
			Map.entry("contact.address.mailing.city", "Toronto"),
			Map.entry("contact.address.mailing.province", "Ontario"),
			Map.entry("contact.address.mailing.postalCode", "L8M1J6"),

			Map.entry("identificationDocuments.socialInsuranceNumber", "356-427-583"),
			Map.entry("identificationDocuments.healthCardNumber", "5584-486-673-YM"),
			Map.entry("identificationDocuments.uniqueClientIdentifier", "0000-0000"),

			Map.entry("statusInCanada.status", "Canadian citizen born in Canada"),

			Map.entry("haveDisability", "no"),
			Map.entry("additionalNutritionalNeeds.specialDiet", "yes"),
			Map.entry("additionalNutritionalNeeds.pregnantOrBreastfeeding", "no"),

			//				Social Assistance
			Map.entry("socialAssistance.memberId", "123456789"),
			//								Below comes from UI and SADA should handle this field
			Map.entry("socialAssistance.socialAssistanceReceivedMonth", "JANUARY"),
			Map.entry("socialAssistance.socialAssistanceReceivedYear", "2010"),

			//				Dependent
			Map.entry("careForChildTemporarily", "yes"),

			//				Institution Details
			Map.entry("institution.type", "INSTITUTION_LONG_TERM_CARE"),
			Map.entry("institution.name", "Some long term care"),
			Map.entry("institution.stay", "full-time"),

			//				Sponsor
			Map.entry("sponsor.name.first", "sponsor"),
			Map.entry("sponsor.name.last", "primary"),
			Map.entry("sponsor.monthlyAmount", "100"),
			Map.entry("sponsor.liveWith", "no"),

			// Earned Income
			Map.entry("earnedIncome[0].earnedIncomeType", "partTime"),
			Map.entry("earnedIncome[0].employerName", "Subway"),
			Map.entry("earnedIncome[0].dateOfFirstPay", "2000-01-01"),
			Map.entry("earnedIncome[0].netPay", "200"),
			Map.entry("earnedIncome[1].earnedIncomeType", "partTime"),
			Map.entry("earnedIncome[1].businessName", "My Business"),
			Map.entry("earnedIncome[1].expectedDateOfFirstPay", "2000-02-01"),
			Map.entry("earnedIncome[1].netPay", "200"),


			// Other Income
			Map.entry("otherIncome.pensionDisability.receivingIncome", "yes"),
			Map.entry("otherIncome.pensionDisability.monthlyAmount", "1234.56"),
			Map.entry("otherIncome.otherIncome[0].description", "Spousal Support"),
			Map.entry("otherIncome.otherIncome[0].receivingIncome", "yes"),
			Map.entry("otherIncome.otherIncome[0].monthlyAmount", "9000.01"),
			Map.entry("otherIncome.otherIncome[1].description", "Rent Money"),
			Map.entry("otherIncome.otherIncome[1].receivingIncome", "no"),

			//              Financial Assets
			Map.entry("financialAssets.disposedAssets", "no"),
			Map.entry("financialAssets.cashTotalValue", "10.0"),
			Map.entry("financialAssets.investmentsTotalValue", "20.0"),
			Map.entry("financialAssets.privateTrustTotalValue", "30.0"),
			Map.entry("financialAssets.opgtTotalValue", "40.0"),
			Map.entry("financialAssets.bank[0].description", "CIBC"),
			Map.entry("financialAssets.bank[0].value", "420.0"),
			Map.entry("financialAssets.otherAsset[0].value", "113.0"),
			Map.entry("financialAssets.otherAsset[0].description", "4-leaf clover"),
			Map.entry("financialAssets.otherAsset[1].value", "421.00"),
			Map.entry("financialAssets.otherAsset[1].description", "Gottfried Wilhelm Leibniz rookie card"),
			Map.entry("financialAssets.vehicle[0].value", "2001.0"),
			Map.entry("financialAssets.vehicle[0].make", "Subaru"),
			Map.entry("financialAssets.vehicle[0].year", "2010"),
			Map.entry("financialAssets.vehicle[1].value", "533.0"),
			Map.entry("financialAssets.vehicle[1].make", "Suzuki"),
			Map.entry("financialAssets.vehicle[1].year", "2001"),
			Map.entry("financialAssets.vehicle[1].usedForWork", "yes"),

			//              Eid
			Map.entry("eid.riskStrategyDecision", "true"),
			Map.entry("eid.score", "50"),
			Map.entry("eid.reasonCodes", "45,98"),
			Map.entry("eid.unweightedScore", "100"),

			Map.entry("genderIdentity", "GENDER_NON_BINARY"),
			Map.entry("indigenousIdentity", "YES"),
			Map.entry("indigenousIdentityGroup", "INUIT"),
			Map.entry("racialIdentity", "EAST_SOUTHEAST_ASIA"),
			Map.entry("visibleMinority", "NO"),
			Map.entry("languageOfChoice", "ENGLISH"),

			// ESignature
			Map.entry("esignature.redirectUrl", "https://ontario.ca")
	                                                                              );

	private static final Map<String, String> spouseFacts = Map.ofEntries(
			Map.entry("name.first", "Spouse"),
			Map.entry("name.last", "Fixit"),
			Map.entry("contact.email", "Spouse.fixist@ontario.ca"),
			Map.entry("birth.sex", "Male"),
			Map.entry("birth.date", "2000-01-01"),

			Map.entry("identificationDocuments.socialInsuranceNumber", "380-332-437"),
			Map.entry("identificationDocuments.healthCardNumber", "5584-486-673-YM"),
			Map.entry("identificationDocuments.uniqueClientIdentifier", "0000-1111"),

			Map.entry("statusInCanada.status", "Permanent Resident"),
			Map.entry("statusInCanada.arrivalDate", "2010-01-01"),

			Map.entry("haveDisability", "yes"),
			Map.entry("fullTimeSchoolType", "secondarySchool"),
			Map.entry("additionalNutritionalNeeds.specialDiet", "yes"),
			Map.entry("additionalNutritionalNeeds.pregnantOrBreastfeeding", "yes"),

			Map.entry("socialAssistance.memberId", "123456789"),
			Map.entry("socialAssistance.socialAssistanceReceivedMonth", "MARCH"),
			Map.entry("socialAssistance.socialAssistanceReceivedYear", "2010"),

			Map.entry("institution.type", "INSTITUTION_WOMENS_SHELTER"),
			Map.entry("institution.name", "Some shelter"),
			Map.entry("institution.stay", "part time"),

			Map.entry("sponsor.name.first", "sponsor"),
			Map.entry("sponsor.name.last", "spouse"),
			Map.entry("sponsor.monthlyAmount", "200"),
			Map.entry("sponsor.liveWith", "yes"),

			// wda
			Map.entry("genderIdentity", "MAN_OR_BOY"),
			Map.entry("indigenousIdentity", "NO"),
			Map.entry("racialIdentity", "BLACK"),
			Map.entry("visibleMinority", "NO"),
			Map.entry("languageOfChoice", "ENGLISH"),

			// ESignature
			Map.entry("esignature.redirectUrl", "https://www.ontario.ca/")
	                                                                    );

	public static Map<String, String> getPrimaryApplicantFacts() {

		return primaryApplicantFacts;
	}

	public static Map<String, String> getSpouseFacts() {

		return spouseFacts;
	}

	public static Map<String, String> getChildFacts(String firstName,
	                                                String dob) {

		return Map.ofEntries(
				Map.entry("name.first", firstName),
				Map.entry("name.last", "Fixit"),
				Map.entry("contact.email", firstName + ".fixist@ontario.ca"),
				Map.entry("birth.sex", "Male"),
				Map.entry("birth.date", dob),

				// ESignature
				Map.entry("esignature.redirectUrl", "https://www.ontario.ca/")
		                    );
	}

}
