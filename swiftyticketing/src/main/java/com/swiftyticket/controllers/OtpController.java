package com.swiftyticket.controllers;

import com.swiftyticket.services.implementations.SmsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.dto.otp.OtpRequest;
import com.swiftyticket.dto.otp.OtpResponseDto;
import com.swiftyticket.dto.otp.OtpStatus;
import com.swiftyticket.dto.otp.OtpValidationRequest;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/otp")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class OtpController {

	@Autowired
	private SmsServiceImpl smsService;
	
	@GetMapping("/process")
	public String processSMS() {
		return "SMS sent";
	}

	@PostMapping("/send")
	public ResponseEntity<OtpResponseDto> sendOtp(@RequestBody @Valid OtpRequest otpRequest) {
		//log will print to console when this command is executed
		log.info("inside sendOtp to "+otpRequest.getEmail());
		OtpResponseDto response = smsService.sendSMS(otpRequest);
		log.info(""+response.getStatus().equals(OtpStatus.DELIVERED));
		return new ResponseEntity<OtpResponseDto>(response, HttpStatus.OK);
	}
	
	@PostMapping("/validate")
    public ResponseEntity<String> validateOtp(@RequestBody @Valid OtpValidationRequest otpValidationRequest) {
		log.info("inside validateOtp :: "+otpValidationRequest.getEmail()+" "+otpValidationRequest.getOtpNumber());
		return new ResponseEntity<String>(smsService.validateOtp(otpValidationRequest), HttpStatus.OK);
    }
	
}
