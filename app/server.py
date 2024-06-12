from twilio.rest import Client

# Your Account SID and Auth Token from twilio.com/console
account_sid = "ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"  # Replace with your Account SID
auth_token = "your_auth_token"  # Replace with your Auth Token

client = Client(account_sid, auth_token)

def send_otp(phone_number):
    verification = client.verify.services("VAxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")  # Replace with your Verify Service SID
    verification.verifications.create(to=phone_number, channel="sms")
    return "OTP sent successfully!"

# Example usage
phone_number = "+1234567890"  # Replace with the user's phone number
response = send_otp(phone_number)
print(response)
