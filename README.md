Simple loan implementation

I spent two chunks of two hours for this implementation. I think the most difficult part is to understand the business context and understand what to expect and test with the sample input/output files. 

RUN INSTRUCTION AND VERIFICATION:

1. Clone the project: git clone https://github.com/liwei2001/loans.git
2. To verify the output for the large data set, go to 'target' folder, assignments.csv and yields.csv are generated.
3. If you have Docker installed, run the script: ./build.sh , it will build and run, the two output files will get generated in the same location in target folder.
4. If you don't have Docker installed, import the project into IDE such as Intellij, open and run LoanApplication.java . 


To EXTEND the covenant coverage, we should define covenant types:
enum CovenantType {
  MAX_DEFAULT_LIKELYHOOD, STATES_RESTRICTION, MAX_LOAN_AMOUNT, MAX_LOAN_LENGTH_IN_YEARS, ...
}

Sample input:
bankId, facilityId, covenantType, covenantValue
1, 1, 0, 0.05 \n
1, 2, 0, 0.08 \n
1, 1, 1, VT CA OR \n
1, 2, 1, AZ TX \n
1, 1, 2, 50000 \n
1, 2, 2, 100000 \n
1, 1, 3, 15 \n
1, 2, 3, 30 \n
........

ADDING NEW FACILITIES:

This program is simply processing loans from a input file. We can implement the real-time loan request processing as a microservice with Kafka (add loan request to a certain Kafka topic) and Kafka Streams (stream processing). To accommodate new facilities addition, we may need to halt the microservice temporarily. After the addition is complete, we can resume the microservice, it will pick up from where it was left off in the Kakfa topic queue (processing offset in the Kafka topic was recorded). The only impact is that the loan request coming during the facilities addition process will get delayed.

The facilities/banks/covenants update can be separate microservice(s). The new records will be appended to the in-memory data structure in Redis (for example) for faster recovery of loan request processing. 


LOAN BATCH ASSIGNMENT:

a. sort facilities by interest rate (cheapest loan guarantee) and fetch the first facility
b. find the matching covenant(s) with the facilityId, or bankId (if facilityId is absent)
c. sort loans by default likelihood and then loan amount, filter by status 'unprocessed' (depending on the criteria for granting loans, sorting columns and order can be different)
d. group the loans with default likelihood less than the first covenant entry, not in the banned state in the first covenant entry, up to the loan capacity of the current facility. Get the loan set granted by this covenant.
e. move to the next matching convenant, iterate remove unqualified loan (not meeting covenant requirement) if necessary
f. mark the loan status as 'granted' in the final set after matching against all relevant convenants. Loans not granted still stays in status 'unprocessed' to participate the next facility iteration.
g. move to the next facility, iterate.


REST APIs:

The facilities/banks/covenants/loans artifacts can be persisted and query through REST APIs:
POST /loanRequest
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
b. facilities are sorted by interest rate, the facility/loan matching will start with the facility with lowest interest rate (cheapest loan guarantee) to see if all covenants are met
c. The best case of loan matching is the first facility (with lowest interest rate) met all the covenants. The total running time is O(len(loans) x len(convenants)). The worst case of loan matching is go through all facilities, the complexity is O(len(facilities) x len(convenants)). The total running time is O(len(loans) x len(facilities) x len(convenants))

d. The batch processing can be more efficient, it's independent of the number of loans: O(len(facilities) x len(covenants)). However, the grouping/sorting on the loans may impose higher complexity: O(len(loans) lg len(loans)).
