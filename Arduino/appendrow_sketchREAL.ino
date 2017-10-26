/* Setup shield-specific #include statements */
#include <SPI.h>
#include <WiFi.h>
#include <WiFiClient.h>
#include <Temboo.h>
#include "TembooAccount.h" // Contains Temboo account information
#include <Wire.h>
#include <Adafruit_MLX90614.h>

Adafruit_MLX90614 mlx = Adafruit_MLX90614();
WiFiClient client;

// The number of times to trigger the action if the condition is met
// We limit this so you won't use all of your Temboo calls while testing
int maxCalls = 10;
float tempC = 0.00;
//int reading;
//float referenceVoltage;;
// The number of times this Choreo has been run so far in this sketch
int calls = 0;
//int inputPin = A0;
float temp_limit = 32.05;
void setup() {
  Serial.begin(9600);
  
  // For debugging, wait until the serial console is connected
  delay(4000);
  while(!Serial);

  int wifiStatus = WL_IDLE_STATUS;

  // Determine if the WiFi Shield is present
  Serial.print("\n\nShield:");
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("FAIL");

    // If there's no WiFi shield, stop here
    while(true);
  }

  Serial.println("OK");

  // Try to connect to the local WiFi network
  while(wifiStatus != WL_CONNECTED) {
    Serial.print("WiFi:");
    wifiStatus = WiFi.begin(WIFI_SSID, WPA_PASSWORD);

    if (wifiStatus == WL_CONNECTED) {
      Serial.println("OK");
    } else {
      Serial.println("FAIL");
    }
    delay(5000);
  }
  

  //Initialize pins
//  pinMode(inputPin, INPUT);
  mlx.begin();  

}

void loop() {
  tempC = mlx.readObjectTempC();

  
//  float tempC = analogRead(inputPin);
//  tempC = (5.0 * tempC * 100.0) / 1024.0; //convert the analog data to temperature
//  Serial.println("Temperature: " + String(tempC));

  
   Serial.print("Object = "); Serial.print(mlx.readObjectTempC()); Serial.println("*C");
   
  
 // if (tempC != 0) {
   
   Serial.print("Call count");Serial.print(calls);
  Serial.println(); 
    if (calls < maxCalls) {
      runAppendRow(tempC);
      calls++;
    } else {

    }
//  }
  
  
  if (tempC > temp_limit ) {
     Serial.println("\nTriggered! Calling SendSMS Choreo...");
      runSendSMS(tempC);
      calls++;
      
  }

  delay(5000);
}

void runAppendRow(float tempC) {
  TembooChoreo AppendRowChoreo(client);

  // Set Temboo account credentials
  AppendRowChoreo.setAccountName(TEMBOO_ACCOUNT);
  AppendRowChoreo.setAppKeyName(TEMBOO_APP_KEY_NAME);
  AppendRowChoreo.setAppKey(TEMBOO_APP_KEY);

  // Set profile to use for execution
  AppendRowChoreo.setProfile("appendrow");
  // Set Choreo inputs
  
  String RowDataValue = "";
  RowDataValue = RowDataValue + String(tempC);
  AppendRowChoreo.addInput("RowData", RowDataValue);
  
  
  // Identify the Choreo to run
  AppendRowChoreo.setChoreo("/Library/Google/Spreadsheets/AppendRow");

  // Run the Choreo
  unsigned int returnCode = AppendRowChoreo.run();
  Serial.println("Choreo Run OK!!!");
  // Read and print the error message
  while (AppendRowChoreo.available()) {
    char c = AppendRowChoreo.read();

  }

  AppendRowChoreo.close();
}




void runSendSMS(float tempC) {
  TembooChoreo SendSMSChoreo(client);

  // Set Temboo account credentials
  SendSMSChoreo.setAccountName(TEMBOO_ACCOUNT);
  SendSMSChoreo.setAppKeyName(TEMBOO_APP_KEY_NAME);
  SendSMSChoreo.setAppKey(TEMBOO_APP_KEY);

  // Set profile to use for execution
  SendSMSChoreo.setProfile("twilio");
  // Identify the Choreo to run
  SendSMSChoreo.setChoreo("/Library/Twilio/SMSMessages/SendSMS");

  // Run the Choreo
  unsigned int returnCode = SendSMSChoreo.run();

  // Read and print the error message
  while (SendSMSChoreo.available()) {
    char c = SendSMSChoreo.read();
    Serial.print(c);
  }
  Serial.println();
  SendSMSChoreo.close();
}
