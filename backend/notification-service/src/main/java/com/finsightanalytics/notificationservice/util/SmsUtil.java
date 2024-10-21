package com.finsightanalytics.notificationservice.util;

import com.finsightanalytics.notificationservice.config.TwilioConfig;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmsUtil {

    @Autowired
    private TwilioConfig twilioConfig;

    public void sendSms(String toPhoneNumber, String message) {
        Message.creator(
                new PhoneNumber(toPhoneNumber), // To phone number
                new PhoneNumber(twilioConfig.getFromPhoneNumber()), // From phone number
                message
        ).create();
    }
}
