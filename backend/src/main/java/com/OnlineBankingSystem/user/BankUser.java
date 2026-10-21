package com.OnlineBankingSystem.user;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "BANK_USER")
public class BankUser {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernateSequence")
	@SequenceGenerator(
			name = "hibernateSequence",
			sequenceName = "HIBERNATE_SEQUENCE",
			allocationSize = 1)
	@Column(name = "USER_ID", nullable = false)
	private Long id;

	@Column(name = "USER_NAME", nullable = false, unique = true, length = 30)
	private String userName;

	@Column(name = "PASSWORD", nullable = false, length = 60)
	private String password;

	@Column(name = "FIRST_NAME", nullable = false, length = 50)
	private String firstName;

	@Column(name = "LAST_NAME", nullable = false, length = 50)
	private String lastName;

	@Column(name = "MIDDLE_INITIAL", length = 1)
	private String middleInitial;

	@Column(name = "GENDER", nullable = false, length = 1)
	private String gender;

	@Column(name = "DATE_OF_BIRTH", nullable = false)
	private LocalDate dateOfBirth;

	@Column(name = "STREET", nullable = false, length = 100)
	private String street;

	@Column(name = "CITY", nullable = false, length = 40)
	private String city;

	@Column(name = "STATE", nullable = false, length = 40)
	private String state;

	@Column(name = "ZIP", nullable = false, length = 10)
	private String zip;

	@Column(name = "PHONE", nullable = false, length = 20)
	private String phone;

	@Column(name = "EMAIL", nullable = false, length = 80)
	private String email;

	protected BankUser() {
	}

	public BankUser(
			String userName,
			String password,
			String firstName,
			String lastName,
			String middleInitial,
			String gender,
			LocalDate dateOfBirth,
			String street,
			String city,
			String state,
			String zip,
			String phone,
			String email) {
		this.userName = userName;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleInitial = middleInitial;
		this.gender = gender;
		this.dateOfBirth = dateOfBirth;
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phone = phone;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getMiddleInitial() {
		return middleInitial;
	}

	public String getGender() {
		return gender;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getZip() {
		return zip;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public void updateProfile(
			String firstName,
			String lastName,
			String middleInitial,
			String gender,
			LocalDate dateOfBirth,
			String street,
			String city,
			String state,
			String zip,
			String phone,
			String email) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.middleInitial = middleInitial;
		this.gender = gender;
		this.dateOfBirth = dateOfBirth;
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phone = phone;
		this.email = email;
	}
}
