package com.swiftyticket.otpApp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swiftyticket.otpApp.dto.OtpRequest;
import com.swiftyticket.otpApp.dto.OtpResponseDto;
import com.swiftyticket.otpApp.dto.OtpValidationRequest;
import com.swiftyticket.otpApp.service.SmsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/otp")
@Slf4j
public class OtpController {

	@Autowired
	private SmsService smsService;
	
	@GetMapping("/process")
	public String processSMS() {
		return "SMS sent";
	}

	@PostMapping("/send-otp")
	public ResponseEntity<OtpResponseDto> sendOtp(@RequestBody OtpRequest otpRequest) {
		//log will print to console when this command is executed
		log.info("inside sendOtp to "+otpRequest.getUsername());
		return new ResponseEntity<OtpResponseDto>(smsService.sendSMS(otpRequest), HttpStatus.OK);
	}
	
	@PostMapping("/validate-otp")
    public ResponseEntity<String> validateOtp(@RequestBody OtpValidationRequest otpValidationRequest) {
		log.info("inside validateOtp :: "+otpValidationRequest.getUsername()+" "+otpValidationRequest.getOtpNumber());
		return new ResponseEntity<String>(smsService.validateOtp(otpValidationRequest), HttpStatus.OK);
    }
	
}
