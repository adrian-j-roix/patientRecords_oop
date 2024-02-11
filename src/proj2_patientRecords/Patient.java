/* Juna Roix
 * W24_COP2552_0M1
 * Professor Sullivan
 * Spring 2024
 * 
 * Project 2 - Patient Records
 * Read-in 3 files to write-out two files simultaneously using strings without a delimiter.
 */
package proj2_patientRecords;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Patient {
	/* FIELDS */
	private String _currentPatientListFilePath;
	private String _removePatientListFilePath;
	private String _newPatientListFilePath;
	private String _updatedPatientListFilePath;
	private String _patientErrorsFilePath;
	
	/* PROPERTIES */
	private String getCurrentSystemDateMmddyyyy() {
		LocalDate date = LocalDate.now();
		DateTimeFormatter MmddyyyyFormatter = DateTimeFormatter.ofPattern("MMddyyyy");
		String MmddyyyyDate = date.format(MmddyyyyFormatter);
		return MmddyyyyDate;
	}
	private Integer getPatientId(String patient) {
		int i; 
		for (i = 0; i < patient.length(); i++) {
            char currentChar = patient.charAt(i);
            if (!Character.isDigit(currentChar)) {
            	break;
			}
        }
		int patientId = tryParseInt(trySubstring(patient, 0, i));
		return patientId;
	}
	
	/* METHODS */
	public Integer tryParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }
	public String trySubstring(String str, int startIndex, int endIndex) {
		try {
			return str.substring(startIndex, endIndex);
			
		} catch (Exception e) {
			return "";
		}
	}
	private String readLines(BufferedReader reader, int lineCount) throws IOException {
		String line;
		String lines = "";
		for (int i = 0; i < lineCount; i++) {
			line = reader.readLine();
			if (line == null ) {
				return null;
			} 
			lines += line + "\n";
		}
		return lines;
	}
	private BufferedWriter createPatientFileWriterIfNull(BufferedWriter writer, String filePath, Boolean addDateHeader) throws IOException {
		if (writer == null) {
			writer = new BufferedWriter(new FileWriter(filePath));
			if (addDateHeader) {
				writer.write(getCurrentSystemDateMmddyyyy() + "\n");
			}
		}
		return writer;
	}
	public void createUpdatedPatientListFile() {
		
		try (
			// PatientList[x].txt
			BufferedReader previousPatientListReader = new BufferedReader(new FileReader(this._currentPatientListFilePath));
			// RemovePatientList.txt
			BufferedReader removePatientListReader = new BufferedReader(new FileReader(this._removePatientListFilePath));
			// NewPatientList.txt
			BufferedReader newPatientListReader = new BufferedReader(new FileReader(this._newPatientListFilePath));
			// PatientList[x+1].txt
			BufferedWriter updatedPatientListWriter = new BufferedWriter(new FileWriter(this._updatedPatientListFilePath));
		) {
			// PatientErrors[x+1].txt (may not need to be created)
			BufferedWriter patientErrorsWriter = null;
			// Write date header for the new PatientListW[x+1].txt
			updatedPatientListWriter.write(getCurrentSystemDateMmddyyyy() + "\n");  
			// Skip the date header in the current PatientListW[x].txt
			previousPatientListReader.readLine();  
			// From PatientListW[x].txt, read-in and store the first patient's ID, 
			// first+last name, and DOB, separated by "\n" characters
			String currentPatient = readLines(previousPatientListReader, 3);
			// Still from PatientList[x].txt, read-in and store 
			// the year the patient was added in an int variable, 
			// per project requirement
			int currentPatient_yearAdded = tryParseInt(previousPatientListReader.readLine());
			// From RemovePatientList.txt, read-in and store the ID, name, 
			// and DOB of the first patient 
			String removePatient = readLines(removePatientListReader, 3);
			// From NewPatientList.txt, read-in and store the ID, name, 
			// and DOB of the first patient
			String newPatient = readLines(newPatientListReader, 3);
			// Continue until all the patients from PatientListW[x].txt have been processed.
			// If the next patient from either (Remove & New)PatientList.txt equals null,
			// the conditional tests will stop performing operations with them
			do { 
				// If currentPatientId is greater than removePatientId
				if (removePatient != null && Objects.compare(getPatientId(currentPatient),getPatientId(removePatient), Integer::compare) > 0) {
					patientErrorsWriter = createPatientFileWriterIfNull(patientErrorsWriter, this._patientErrorsFilePath, true);
					// Write current removePatient to PatientErrorsW[x+1].txt
					patientErrorsWriter.write(removePatient);
					// Read-in next removePatient from RemovePatientList.txt
					removePatient = readLines(removePatientListReader, 3);
				} 
				// Else if currentPatientId equals removePatientId
				else if (removePatient != null && (Objects.equals(getPatientId(currentPatient), getPatientId(removePatient)))) {
					// Prevent writing currentPatient to PatientListW[x+1].txt
					currentPatient = readLines(previousPatientListReader, 3);
					// Store next currentPatient in PatientListW[x].txt
					currentPatient_yearAdded = tryParseInt(previousPatientListReader.readLine());
					// Store next removePatient in RemovePatient.txt
					removePatient = readLines(removePatientListReader, 3);
				} 
				// Else if currentPatientId equals newPatientId
				else if (newPatient != null && Objects.equals(getPatientId(currentPatient), getPatientId(newPatient))) {
					patientErrorsWriter = createPatientFileWriterIfNull(patientErrorsWriter, this._patientErrorsFilePath, true);
					// Write current newPatient to PatientErrorsW[x+1].txt
					patientErrorsWriter.write(newPatient);
					// Store next newPatient in NewPatientList.txt
					newPatient = readLines(newPatientListReader, 3);
				} 
				// Else if currentPatientId is greater than newPatientId
				else if (newPatient != null && Objects.compare(getPatientId(currentPatient),getPatientId(newPatient), Integer::compare) > 0) {
					// Write current newPatient to PatientListW[x+1].txt
					updatedPatientListWriter.write(newPatient + getCurrentSystemDateMmddyyyy().substring(4, 8) + "\n");
					// Store next newPatient in NewPatientList.txt
					newPatient = readLines(newPatientListReader, 3);
				} 
				else {
					// Write the currentPatient to PatientListW[x+1].txt
					updatedPatientListWriter.write(currentPatient + currentPatient_yearAdded + "\n");
					// Store next patient in PatientListW[x].txt
					currentPatient = readLines(previousPatientListReader, 3);
					currentPatient_yearAdded = tryParseInt(previousPatientListReader.readLine());
				}
			} while (currentPatient != null);
			while (newPatient != null) {
				updatedPatientListWriter.write(newPatient + getCurrentSystemDateMmddyyyy().substring(4, 8) + "\n");
				newPatient = readLines(newPatientListReader, 3);
			}
			// If removePatient still is not null, write the remaining patients 
			// in RemovePatientList.txt to PatientErrorsW[x+1].txt
			while (removePatient != null) {
				patientErrorsWriter = createPatientFileWriterIfNull(patientErrorsWriter, this._patientErrorsFilePath, true);
				patientErrorsWriter.write(removePatient);
				removePatient = readLines(removePatientListReader, 3);
			}
			if (patientErrorsWriter != null) {
				patientErrorsWriter.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	/* CONSTRUCTORS */
	public Patient(
			String currentPatientListFilePath, String removePatientListFilePath, 
			String newPatientListFilePath, String updatedPatientListFilePath, 
			String patientErrorsFilePath 
	) {
		this._currentPatientListFilePath = currentPatientListFilePath;
		this._removePatientListFilePath = removePatientListFilePath;
		this._newPatientListFilePath = newPatientListFilePath;
		this._updatedPatientListFilePath = updatedPatientListFilePath;
		this._patientErrorsFilePath = patientErrorsFilePath;
	}
	
	/* MAIN */
	public static void main(String[] args) {
		// Current Working Directory
		String currentPatientListFilename = "PatientListW3.txt";
		String removePatientListFilename = "RemovePatientList.txt";
		String newPatientListFilename = "NewPatientList.txt";
		String updatedPatientListFilename = "PatientListW4.txt";
		String patientErrorsFilename = "PatientErrorsW4.txt";
		String mergeFilename = "UpdatedPatientListMerge.txt";
		// Absolute File Paths
		String updatedPatientListFilePath = "C:\\SFC\\COP2552\\Project2\\PatientListW4.txt";
		String patientErrorsFilePath = "C:\\SFC\\COP2552\\Project2\\PatientErrorsW4.txt";
		// Test Cases
//		// 0
//		Patient patient = new Patient(currentPatientListFilename, removePatientListFilename, newPatientListFilename, updatedPatientListFilename, patientErrorsFilename);
//		patient.createUpdatedPatientListFile();
//		// 1
//		Patient patient_tc_1 = new Patient("TC_1/" + currentPatientListFilename, "TC_1/" + removePatientListFilename, "TC_1/" + newPatientListFilename, "TC_1/" + updatedPatientListFilename, "TC_1/" + patientErrorsFilename);
//		patient_tc_1.createUpdatedPatientListFile();
//		// 2
//		Patient patient_tc_2 = new Patient("TC_2/" + currentPatientListFilename, "TC_2/" + removePatientListFilename, "TC_2/" + newPatientListFilename, "TC_2/" + updatedPatientListFilename, "TC_2/" + patientErrorsFilename);
//		patient_tc_2.createUpdatedPatientListFile();
		// Submission
		Patient proj2_submission_patient = new Patient(currentPatientListFilename, removePatientListFilename, newPatientListFilename, updatedPatientListFilePath, patientErrorsFilePath);
		proj2_submission_patient.createUpdatedPatientListFile();
    }
}
