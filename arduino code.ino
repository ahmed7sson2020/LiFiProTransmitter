#define Solenoid 12
#define ldr A0
#define LaserReceiver A1
#define LaserEmitter 7
int Buzzer = 4;
int GreenLed = 6;
int RedLed = 11;

int val;
int val2;
String duration;
bool doorOpen = false;

int baselineLDR = 0;
int thresholdDifference = 100; 

void setup() {
  Serial.begin(9600);
  pinMode(Solenoid, OUTPUT);
  pinMode(GreenLed, OUTPUT);
  pinMode(RedLed, OUTPUT);
  pinMode(LaserEmitter, OUTPUT);
  pinMode(Buzzer, OUTPUT);

  digitalWrite(RedLed, HIGH);
  digitalWrite(LaserEmitter, HIGH);

  delay(1000);
  baselineLDR = analogRead(ldr);
  Serial.print("Baseline LDR: ");
  Serial.println(baselineLDR);
}

void OpenDoor(){            
  digitalWrite(Solenoid, HIGH);
  tone(Buzzer, 500);
  digitalWrite(RedLed, LOW);
  digitalWrite(GreenLed, HIGH);
  digitalWrite(LaserEmitter, LOW);
  doorOpen = true;
  delay(3000);
  digitalWrite(Solenoid, LOW);
  noTone(Buzzer);
  digitalWrite(RedLed, HIGH);
  digitalWrite(GreenLed, LOW);
  digitalWrite(LaserEmitter, HIGH);
  doorOpen = false;
}

void loop() {
  val = (baselineLDR - analogRead(ldr) > thresholdDifference) ? 0 : 1;

  while(val == 0)
  {
    val2 = (baselineLDR - analogRead(ldr) > thresholdDifference) ? 0 : 1;
    duration += val2;

    if(duration == "0001")
    {
      OpenDoor();
    }

    if(val2 == 1)
    {
      duration = "";
      break;
    }

    delay(200);
  }

  int laserValue = analogRead(LaserReceiver);

  if (!doorOpen && laserValue > 500)
  {
    tone(Buzzer, 1000);
  }
  else
  {
    noTone(Buzzer);
  }
}
