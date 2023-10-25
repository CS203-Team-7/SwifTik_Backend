package com.swiftyticket.services.implementations;

import java.text.DecimalFormat;
import java.util.*;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swiftyticket.config.TwilioConfig;
import com.swiftyticket.dto.otp.OtpRequest;
import com.swiftyticket.dto.otp.OtpResponseDto;
import com.swiftyticket.dto.otp.OtpStatus;
import com.swiftyticket.dto.otp.OtpValidationRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import com.swiftyticket.repositories.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl {
	private final UserRepository userRepo;

	@Autowired
	private TwilioConfig twilioConfig;
	Map<String, String> otpMap = new HashMap<>();

	/**
	 * This method is called when the bean is created, and it initializes the Twilio
	 * account with the account SID and auth token.
	 */
	@PostConstruct
	public void setup() {
		Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
	}

	/**
	 * This method generates a random 6-digit OTP for the user.
	 * @return String otp -> 6-digit OTP
	 */
	public String generateOTP() {
		return new DecimalFormat("000000")
				.format(new SecureRandom().nextInt(999999));
	}

	/**
	 * This method sends an SMS to the user's phone number with the OTP generated.
	 * @param otpRequest -> OtpRequest object containing the user's email and phone number
	 * @throws Exception -> if the SMS fails to send
	 * @return OtpResponseDto -> OtpResponseDto object containing the status of the SMS
	 */
	public OtpResponseDto sendSMS(OtpRequest otpRequest) {
		OtpResponseDto otpResponseDto = null;
		try {
			// get the to and from number for the message function later
			PhoneNumber to = new PhoneNumber(otpRequest.getPhoneNumber());// to
			PhoneNumber from = new PhoneNumber(twilioConfig.getPhoneNumber()); // from

			// generate OTP&message using the earlier method
			String otp = generateOTP();
			String otpMessage = "Hi, " + otpRequest.getEmail() + ", your OTP for verification is: " + otp
					+ "\r\nbeep boop I am a bot please do not reply to this number";

			Message
					.creator(to, from,
							otpMessage)
					.create();

			log.info("OTP Sent to " + otpRequest.getEmail() + ", Otp:" + otp);

			// store in map with username as key, and save response in the DTO so we can
			// access it later back in the controller.
			otpMap.put(otpRequest.getEmail(), otp);
			otpResponseDto = new OtpResponseDto(OtpStatus.DELIVERED, otpMessage);
		} catch (Exception e) {
			otpResponseDto = new OtpResponseDto(OtpStatus.FAILED, e.getMessage());
		}
		return otpResponseDto;
	}

	/**
	 * This method checks if the OTP entered by the user is correct.
	 * @param otpValidationRequest -> OtpValidationRequest object containing the user's email and OTP
	 * @return String message -> message to indicate success or failure
	 */
	public String validateOtp(OtpValidationRequest otpValidationRequest) {
		// get corresponding given otp for username trying to verify
		String username = otpValidationRequest.getEmail();
		String given_otp = otpMap.get(username);
		String entered_otp = otpValidationRequest.getOtpNumber();

		if (entered_otp.equals(given_otp)) {
			otpMap.remove(username, otpValidationRequest.getOtpNumber());

			// set the user to verified
			userRepo.enableAppUser(otpValidationRequest.getEmail());
			return "Success! You may log into your account now";
		} else {
			return "OTP is invalid! Please try again, or request for a new OTP";
		}
	}
}
