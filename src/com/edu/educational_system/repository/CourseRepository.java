package com.edu.educational_system.repository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.List;

import com.edu.educational_system.model.*;
import com.edu.educational_system.exception.CourseWasNotFoundException;

public class CourseRepository {

	private final File FILE_PATH = new File("course.txt");

	public CourseRepository() {
		try {
			if (!FILE_PATH.exists()) {
				FILE_PATH.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Не удалось создать файл course.txt", e);
		}
	}

	public void saveCourse(Course course) throws CourseWasNotFoundException {
		List<Course> existingCourses = loadCoursesFromFile();

		for (Course exCourse : existingCourses) {
			if (exCourse.getName().equalsIgnoreCase(course.getName())) {
				throw new IllegalArgumentException("Course with name '" + course.getName() + "' already exists");
			}
		}

		try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FILE_PATH, true)))) {
			pw.println("Course;" + course.getName());

			for (Person p : course.getParticipants()) {
				pw.println(serializePerson(p));
			}

			for (Person p : course.getStaff()) {
				pw.println(serializePerson(p));
			}

			pw.println("---");

		} catch (IOException e) {
			throw new RuntimeException("Ошибка при записи курса в файл: " + e.getMessage(), e);
		}
	}

	public List<Course> getAllCourses() throws CourseWasNotFoundException {
		return loadCoursesFromFile();
	}

	public List<Course> loadCoursesFromFile() throws CourseWasNotFoundException {
		List<Course> result = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
			Course course = null;
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.equals("---") || line.isBlank())
					continue;

				String[] parts = line.split(";");
				switch (parts[0]) {
				case "Course" -> {
					if (course != null)
						result.add(course);
					course = new Course(parts[1]);
				}
				case "Teacher", "Administrator", "Student" -> {
					if (course != null) {
						Person person = deserializePerson(parts);
						course.addParticipant(person);
					}
				}
				}
			}
			if (course != null)
				result.add(course);

		} catch (IOException e) {
			throw new CourseWasNotFoundException("Не удалось загрузить курсы: " + e.getMessage());
		}

		return result;
	}

	public void obfuscateStudentInCourse(String courseName, String studentEmail) {
        List<String> originalLines = readAllLines();
        List<String> updatedLines = new ArrayList<>();
        boolean insideCourse = false;

        for (String line : originalLines) {
            if (line.startsWith("Course;")) {
                insideCourse = line.equalsIgnoreCase("Course;" + courseName);
            } else if (insideCourse && line.startsWith("Student;")) {
                String[] parts = line.split(";");
                if (parts.length >= 5 && parts[2].equalsIgnoreCase(studentEmail)) {
                    line = "Student;***;***@***;" + parts[3] + ";" + parts[4];
                }
            }
            updatedLines.add(line);
        }

        writeAllLines(updatedLines);
    }
	
	private String serializePerson(Person p) {
        if (p instanceof Student s) {
            return "Student;" + s.getName() + ";" + s.getEmail() + ";" + s.getGroup() + ";" + s.getAverageGrade();
        } else if (p instanceof Administrator a) {
            return "Administrator;" + a.getName() + ";" + a.getEmail() + ";" + a.getDepartment();
        } else if (p instanceof Teacher t) {
            return "Teacher;" + t.getName() + ";" + t.getEmail() + ";" + t.getSubject();
        }
        return "";
    }
	
	private Person deserializePerson(String[] parts) {
        return switch (parts[0]) {
            case "Student" -> new Student(parts[1], parts[2], parts[3], Double.parseDouble(parts[4]));
            case "Teacher" -> new Teacher(parts[1], parts[2], parts[3]);
            case "Administrator" -> new Administrator(parts[1], parts[2], parts[3]);
            default -> null;
        };
    }
	
	private List<String> readAllLines() {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return lines;
    }
	
	private void writeAllLines(List<String> lines) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }
	
	
	
}


