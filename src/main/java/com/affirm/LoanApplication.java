package com.affirm;

import com.affirm.data.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LoanApplication {

    private static final Logger log = Logger.getLogger(LoanApplication.class);

    private final static String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    List<Bank> banks;
    List<Facility> facilities;
    List<Covenant> covenants;
    List<Loan> loans;
    List<Assignment> assignments;

    public void setup() {
        banks = processInputFile("/banks.csv", mapToBankItem);
        facilities = processInputFile("/facilities.csv", mapToFacilityItem);
        Collections.sort(facilities, new Comparator<Facility>() {
            @Override
            public int compare(Facility f1, Facility f2) {
                return Math.round((f1.getInterestRate() - f2.getInterestRate()) * 100);
            }
        });

        covenants = processInputFile("/covenants.csv", mapToCovenantItem);

        loans = processInputFile("/loans.csv", mapToLoanItem);
    }

    public static void main(String [] args) {
        LoanApplication loanApplication = new LoanApplication();

        loanApplication.setup();
        loanApplication.streamProcessingLoans();
    }

    private <T> List<T> processInputFile(String inputFile, Function<String, T> mapFunction) {

        List<T> inputList = new ArrayList<T>();

        try {
            InputStream inputFS = getClass().getResourceAsStream(inputFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
            // skip the header of the csv
            inputList = br.lines().skip(1).map(mapFunction).collect(Collectors.toList());
            br.close();
        } catch (Exception e) {
            log.debug("reading input file exception from: " + inputFile);
            e.printStackTrace();
        }

        return inputList;
    }

    private Function<String, Bank> mapToBankItem = (line) -> {
        String[] input = line.split(COMMA_DELIMITER);
        return new Bank(Integer.parseInt(input[0]), input[1]);
    };

    private Function<String, Facility> mapToFacilityItem = (line) -> {
        String[] input = line.split(COMMA_DELIMITER);
        return new Facility(Integer.parseInt(input[2]), Integer.parseInt(input[3]),
                            Float.parseFloat(input[1]), Math.round(Float.parseFloat(input[0])));
    };

    private Function<String, Covenant> mapToCovenantItem = (line) -> {
        String[] input = line.split(COMMA_DELIMITER);
        //if facilityId field value is empty in csv file, assign -1 as facilityId value for this covenant object
        //if max allowed default likelihood is empty, then assign maxDefaultRate value as 1 (100%)
        return new Covenant(Integer.parseInt(input[2]), StringUtils.isEmpty(input[0])? -1 : Integer.parseInt(input[0]),
                            StringUtils.isEmpty(input[1])? 1 : Float.parseFloat(input[1]), input[3]);
    };

    private Function<String, Loan> mapToLoanItem = (line) -> {
        String[] input = line.split(COMMA_DELIMITER);
        return new Loan(Integer.parseInt(input[2]), Integer.parseInt(input[1]),
                        Float.parseFloat(input[0]), Float.parseFloat(input[3]), input[4]);
    };

    public void streamProcessingLoans() {

        assignments = loans.stream().map(mapToAssignmentItem).collect(Collectors.toList());

        generatingOutput();
    }

    private Function<Loan, Assignment> mapToAssignmentItem = (loan) -> {
        //find the first facility whose remaining loan size is no less than the loan amount
        //and meet all the covenant requirements
        Optional<Facility> assignedFacility = facilities.stream().filter(f ->
                (f.getRemainingLoanSize() >= loan.getAmount() && meetAllCovenants(f, loan.getDefaultRate(), loan.getState()))).findFirst();

        if (assignedFacility.isPresent()) {
            //after a facility is located for granting the loan, update the remaining loan size for that facility and the current yields
            updateFacilityRemainingLoanAmount(assignedFacility.get(), loan.getAmount());
            updateFacilityCurrentYield(assignedFacility.get(), loan);
        }

        return new Assignment(loan.getId(), assignedFacility.isPresent() ? assignedFacility.get().getId() : -1);
    };

    private void updateFacilityRemainingLoanAmount(Facility facility, int loanAmount) {
        facility.setRemainingLoanSize(facility.getRemainingLoanSize() - loanAmount);
    }

    private void updateFacilityCurrentYield(Facility facility, Loan loan) {
        /*
        expected_yield =
            (1 - default_likelihood) * loan_interest_rate * amount - default_likelihood * amount
            - facility_interest_rate * amount
         */
        int expected_yield = Math.round((1 - loan.getDefaultRate()) * loan.getInterestRate() * loan.getAmount()
                                - loan.getDefaultRate() * loan.getAmount() - facility.getInterestRate() * loan.getAmount());
        facility.setCurrentYield(facility.getCurrentYield() + expected_yield);
    }

    private boolean meetAllCovenants(Facility facility, float defaultRate, String state) {
        List<Covenant> convenantsMet = covenants.stream().filter(c ->
                ((c.getFacilityId() == -1 ?  //covenant facility id is empty, applies to all facilities for that bank
                        //facilityId is empty: bankId not matching, covenant check passed; otherwise, check banned state and default rate comparison
                        (c.getBankId() != facility.getBankId() ? true : ((StringUtils.isEmpty(c.getBannedState()) || !c.getBannedState().equals(state)) && c.getMaxDefaultRate() >= defaultRate)) :
                        //facilityId is not empty: facilityId not matching, covenant check passed; otherwise, check bank id, banned state and default rate comparison
                        (c.getFacilityId() != facility.getId() ? true : (c.getBankId() == facility.getBankId() && (StringUtils.isEmpty(c.getBannedState()) || !c.getBannedState().equals(state)) && c.getMaxDefaultRate() >= defaultRate)))
                        )).collect(Collectors.toList());

        return convenantsMet.size() == covenants.size();
    }

    private void generatingOutput() {

        FileWriter fileWriter = null;

        try {
            URL res = getClass().getResource("/loans.csv");
            File file = Paths.get(res.toURI()).toFile();
            String absolutePath = file.getAbsolutePath();

            fileWriter = new FileWriter(absolutePath.replace("loans.csv", "assignments.csv"));

            fileWriter.append("loan_id,facility_id");
            fileWriter.append(NEW_LINE_SEPARATOR);
            for (Assignment assignment : assignments) {
                fileWriter.append(String.valueOf(assignment.getLoadId()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(String.valueOf(assignment.getFacilityId()));
                fileWriter.append(NEW_LINE_SEPARATOR);
            }

            fileWriter.flush();
            fileWriter.close();

            fileWriter = new FileWriter(absolutePath.replace("loans.csv", "yields.csv"));

            fileWriter.append("facility_id,expected_yield");
            fileWriter.append(NEW_LINE_SEPARATOR);
            for (Facility facility : facilities) {
                fileWriter.append(String.valueOf(facility.getId()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(String.valueOf(facility.getCurrentYield()));
                fileWriter.append(NEW_LINE_SEPARATOR);
            }

        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {

            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }

    }

}
