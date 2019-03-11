simple loan implementation


I spent two chunks of two hours for this implementation. I think the most difficult part is to understand the business context and analyze what to expect and test with the sample input/output files. The other time-consuming part is the follow-up questions below :).

To EXTEND the covenant coverage, we should define covenant types:
enum CovenantType {
  MAX_DEFAULT_LIKELYHOOD, STATES_RESTRICTION, MAX_LOAN_AMOUNT, MAX_LOAN_LENGTH_IN_YEARS, ...
}

Sample input:
bankId, facilityId, covenantType, covenantValue
1, 1, 0, 0.05
1, 2, 0, 0.08
1, 1, 1, VT CA OR
1, 2, 1, AZ TX
1, 1, 2, 50000
1, 2, 2, 100000
1, 1, 3, 15
1, 2, 3, 30
........

ADDING NEW FACILITIES:

This program is simply processing loans from a input file. We can implement the real-time loan request processing as a microservice with Kafka and Kafka Streams. The microservice will listen to the loan request incoming Kafka topic. To accommodate new facilities addition. We may need to halt the microservice temporarily. After the addition is complete, we can resume the microservice, it will pick up from where it was left off in the Kakfa topic queue (offset was recorded). The only impact is that the loan request coming during the facilities addition process will get delayed.

The facilities/banks/covenants update can be separate microservice(s). The new records will be appended to the in-memory data structure in Redis (for example) for faster recovery of loan request processing. The loan request processing microservice can also be designed to listen to two types of messages in the topic queue: regular loan request message type and the system upgrade message type. The latter can halt regular loan request processing upon incoming system upgrade request message and resume upon system upgrade complete message.


LOAN BATCH ASSIGNMENT:

a. fetch the first facility from sorted list
b. find the matching covenant(s) with the facilityId, or bankId (if facilityId is absent)
c. sort loans by default likelihood, loan amount secondary  (depending on the criteria for granting loans, sorting columns and order can be different)
d. group the loans with default likelihood less than the first covenant entry, not in the banned state in the first covenant entry, up to the loan capacity of the current facility. This group of loans is granted.
e. move to the next matching convenant, grant the next group of loans if possible
f. move to the next facility, iterate.


REST APIs:

The facilities/banks/covenants/loans artifacts can be persisted and query through REST APIs:
POST /loadRequest
{
	"id": {id},
	"amount": {amount},
	"interest_rate": {interest_rate},
	"default_likelihood": {default_likelihood},
	"state": {state},
}
Response:
{
	"status": "Granted" (or "Denied"),
	"facilityId": {facilityId} (or -1 for denied case)
}

GET /facilities/
GET /facilities/{id}
Response:
{
	"id": {id},
	"bankId": {bankId},
	"interestRate": {interestRate},
	"remainingLoanSize": {remainingLoanSize}
}


The RUNTIME complexity:

a. reading from input files: banks, covenants, facilities is linear
b. facilities are sorted by interest rate, the facility/loan matching will start with the facility with lowest interest rate to see if all covenants are met
c. The best case of loan matching is the first facility (with lowest interest rate) met all the covenat. The total running time is O(len(loans) x len(convenants)). The worst case of loan matching is go through all facilities, the complexity is O(len(facilities) x len(convenants)). The total running time is O(len(loans) x len(facilities) x len(convenants))

d. The batch processing can be more efficient, it's independent of the number of loans: O(len(facilities) x len(covenants)). However, the grouping/sorting loans may impose higher complexity: O(len(loans) lg len(loans)).
