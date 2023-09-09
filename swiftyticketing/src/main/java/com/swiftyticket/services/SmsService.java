package com.swiftyticket.services;

import java.text.DecimalFormat;
import java.util.*;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swiftyticket.config.TwilioConfig;
import com.swiftyticket.dto.OtpRequest;
import com.swiftyticket.dto.OtpResponseDto;
import com.swiftyticket.dto.OtpStatus;
import com.swiftyticket.dto.OtpValidationRequest;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;

@Service
public class SmsService {

	@Autowired
	private TwilioConfig twilioConfig;
    Map<String, String> otpMap = new HashMap<>();

	@PostConstruct
	public void setup() {
		Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
	}

	//we use decimal format to makesure the generated number is always 6 digits (will front fill 0's)
	private String generateOTP() {
        return new DecimalFormat("000000")
                .format(new SecureRandom().nextInt(999999));
    }

	public OtpResponseDto sendSMS(OtpRequest otpRequest) {
		OtpResponseDto otpResponseDto = null;
		try {
			//get the to and from number for the message function later
			PhoneNumber to = new PhoneNumber(otpRequest.getPhoneNumber());//to
			PhoneNumber from = new PhoneNumber(twilioConfig.getPhoneNumber()); // from

			//generate OTP&message using the earlier method
			String otp = generateOTP();
			String otpMessage = "Hi, " + otpRequest.getUsername() +  ", your OTP for verification is: " + otp + "\r\nbeep boop I am a bot please do not reply to this number";

			//twilio will send the message here, and return a message
			Message message = Message
			        .creator(to, from,
			                otpMessage)
			        .create();
			
			//store in map with username as key, and save response in the DTO so we can access it later back in the controller.
			otpMap.put(otpRequest.getUsername(), otp);
			otpResponseDto = new OtpResponseDto(OtpStatus.DELIVERED, otpMessage);
		} catch (Exception e) {
			e.printStackTrace();
			otpResponseDto = new OtpResponseDto(OtpStatus.FAILED, e.getMessage());
		}
		return otpResponseDto;
	}
	
	public String validateOtp(OtpValidationRequest otpValidationRequest) {
		//get corresponding given otp for username trying to verify
		String username = otpValidationRequest.getUsername();
		String given_otp = otpMap.get(username);
		String entered_otp = otpValidationRequest.getOtpNumber();

        if (entered_otp.equals(given_otp)) {
            otpMap.remove(username,otpValidationRequest.getOtpNumber());
            return "OTP is valid!";
        } else {
            return "OTP is invalid!";
        }
	}

}
